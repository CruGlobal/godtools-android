package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.room.InvalidationTracker
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.ResponseBody
import org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.support.extractResources
import org.cru.godtools.article.aem.service.support.findAemArticles
import org.cru.godtools.article.aem.util.AemFileSystem
import org.cru.godtools.article.aem.util.addExtension
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber

private const val TAG = "AemArticleManager"

@VisibleForTesting
internal const val CLEANUP_DELAY = HOUR_IN_MS
@VisibleForTesting
internal const val CLEANUP_DELAY_INITIAL = MIN_IN_MS
private const val CACHE_BUSTING_INTERVAL_JSON = HOUR_IN_MS

@VisibleForTesting
internal val QUERY_DOWNLOADED_ARTICLE_TRANSLATIONS = Query.select<Translation>()
    .join(TranslationTable.SQL_JOIN_TOOL)
    .where(ToolTable.FIELD_TYPE.eq(Tool.Type.ARTICLE).and(TranslationTable.SQL_WHERE_DOWNLOADED))

@Singleton
class AemArticleManager @Inject internal constructor(
    private val aemDb: ArticleRoomDatabase,
    private val api: AemApi,
    private val fileManager: FileManager,
    private val manifestManager: ManifestManager
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val aemImportMutex = MutexMap()
    private val articleMutex = MutexMap()
    private val resourceMutex = MutexMap()

    // region Deeplinked Article
    @AnyThread
    suspend fun downloadDeeplinkedArticle(uri: Uri) {
        aemDb.aemImportRepository().accessAemImport(uri)
        syncAemImport(uri, false)
        downloadArticle(uri, false)
    }
    // endregion Deeplinked Article

    // region Translations
    @VisibleForTesting
    internal suspend fun processDownloadedTranslations(translations: List<Translation>) = coroutineScope {
        val repository = aemDb.translationRepository()
        translations.filterNot { repository.isProcessed(it) }.map { translation ->
            launch {
                manifestManager.getManifest(translation)?.let { manifest ->
                    repository.addAemImports(translation, manifest.aemImports)
                    coroutineScope.launch { syncAemImportsFromManifest(manifest, false) }
                }
            }
        }.joinAll()

        // prune any translations that we no longer have downloaded.
        repository.removeMissingTranslations(translations)
    }
    // endregion Translations

    // region AEM Import
    suspend fun syncAemImportsFromManifest(manifest: Manifest?, force: Boolean) = coroutineScope {
        manifest?.aemImports?.forEach { launch { syncAemImport(it, force) } }
    }

    /**
     * This method is responsible for syncing an individual AEM Import url to the AEM Article database.
     *
     * @param baseUri The base AEM Import URL to sync
     * @param force   This flag indicates that this sync should ignore the lastUpdated time
     */
    private suspend fun syncAemImport(baseUri: Uri, force: Boolean) {
        if (!baseUri.isAbsolute || !baseUri.isHierarchical) return
        aemImportMutex.withLock(baseUri) {
            val aemImport = aemDb.aemImportDao().find(baseUri)?.takeIf { force || it.isStale() } ?: return

            // fetch the raw json
            val t = System.currentTimeMillis().let { if (force) it else it.roundTimestamp(CACHE_BUSTING_INTERVAL_JSON) }
            val json = try {
                api.getJson(baseUri.addExtension("9999.json"), t).takeIf { it.code() == HTTP_OK }?.body()
            } catch (e: IOException) {
                Timber.tag(TAG).v(e, "Error downloading AEM json")
                null
            } ?: return

            // parse & store articles
            val articles = json.findAemArticles(baseUri).toList()
            aemDb.aemImportRepository().processAemImportSync(aemImport, articles)

            // launch download of all the articles
            articles.forEach { coroutineScope.launch { downloadArticle(it.uri, false) } }
        }
    }

    // endregion AEM Import

    // region Download Article
    @AnyThread
    suspend fun downloadArticle(uri: Uri, force: Boolean) {
        articleMutex.withLock(uri) {
            // short-circuit if there isn't an Article for the uri or if the article doesn't need to be downloaded
            val article = aemDb.articleDao().find(uri) ?: return
            if (article.uuid == article.contentUuid && !force) return

            // download the article html
            try {
                api.downloadArticle(article.uri.addExtension("html")).takeIf { it.code() == HTTP_OK }?.let { response ->
                    article.apply {
                        contentUuid = uuid
                        content = response.body()
                        resources = extractResources()
                    }
                    aemDb.articleRepository().updateContent(article)
                    aemDb.resourceDao().getAllForArticle(article.uri).downloadResourcesNeedingUpdate()
                }
            } catch (e: IOException) {
                Timber.tag(TAG).d(e, "Error downloading article")
            }
        }
    }
    // endregion Download Article

    // region Download Resource
    @AnyThread
    private fun Collection<Resource>.downloadResourcesNeedingUpdate() {
        filter { it.needsDownload() }.forEach { coroutineScope.launch { downloadResource(it.uri, false) } }
    }

    @AnyThread
    suspend fun downloadResource(uri: Uri, force: Boolean) {
        resourceMutex.withLock(uri) {
            // short-circuit if the resource doesn't exist or it doesn't need to be downloaded
            val resource = aemDb.resourceDao().find(uri)
            if (resource == null || (!force && !resource.needsDownload())) return

            // download the resource
            withContext(Dispatchers.IO) {
                try {
                    api.downloadResource(uri).takeIf { it.code() == HTTP_OK }?.body()
                        ?.let { fileManager.storeResponse(it, resource) }
                } catch (e: IOException) {
                    Timber.tag(TAG).d(e, "Error downloading attachment %s", uri)
                }
            }
        }
    }
    // endregion Download Resource

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal suspend fun shutdown() {
        val job = coroutineScope.coroutineContext[Job]
        if (job is CompletableJob) job.complete()
        job?.join()
    }

    @Singleton
    internal class Dispatcher(
        aemArticleManager: AemArticleManager,
        aemDb: ArticleRoomDatabase,
        dao: GodToolsDao,
        fileManager: FileManager,
        coroutineScope: CoroutineScope
    ) {
        @Inject
        constructor(
            aemArticleManager: AemArticleManager,
            aemDb: ArticleRoomDatabase,
            dao: GodToolsDao,
            fileManager: FileManager
        ) : this(aemArticleManager, aemDb, dao, fileManager, CoroutineScope(Dispatchers.Default))

        // region Translations
        private val articleTranslationsJob = dao.getAsFlow(QUERY_DOWNLOADED_ARTICLE_TRANSLATIONS).conflate()
            .onEach { aemArticleManager.processDownloadedTranslations(it) }
            .launchIn(coroutineScope)
        // endregion Translations

        // region Stale AemImports Refresh
        private val staleAemImportsJob = coroutineScope.launch {
            aemDb.aemImportDao().getAll()
                .filter { it.isStale() }
                .forEach { launch { aemArticleManager.syncAemImport(it.uri, false) } }
        }
        // endregion Stale AemImports Refresh

        // region Cleanup
        @VisibleForTesting
        @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
        internal val cleanupActor = coroutineScope.actor<Unit>(capacity = Channel.CONFLATED) {
            withTimeoutOrNull(CLEANUP_DELAY_INITIAL) { channel.receiveCatching() }
            while (!channel.isClosedForReceive) {
                fileManager.removeOrphanedFiles()
                withTimeoutOrNull(CLEANUP_DELAY) { channel.receiveCatching() }
            }
        }

        init {
            aemDb.invalidationTracker.addObserver(object : InvalidationTracker.Observer(Resource.TABLE_NAME) {
                override fun onInvalidated(tables: Set<String>) {
                    if (Resource.TABLE_NAME in tables) cleanupActor.trySend(Unit)
                }
            })
        }
        // endregion Cleanup

        @RestrictTo(RestrictTo.Scope.TESTS)
        internal fun shutdown() {
            staleAemImportsJob.cancel()
            articleTranslationsJob.cancel()
            cleanupActor.close()
        }
    }

    @Singleton
    internal class FileManager @Inject constructor(
        private val fs: AemFileSystem,
        private val resourceDao: ResourceDao,
    ) {
        private val filesystemMutex = ReadWriteMutex()

        internal suspend fun storeResponse(response: ResponseBody, resource: Resource): File? {
            if (!fs.exists()) return null

            // create a MessageDigest to dedup files
            val digest = try {
                MessageDigest.getInstance("SHA-1")
            } catch (e: NoSuchAlgorithmException) {
                Timber.tag(TAG).d(e, "Unable to create MessageDigest to dedup AEM resources")
                null
            }

            // lock the file system for writing this resource
            filesystemMutex.read.withLock {
                // write the stream to a temporary file
                val tmpFile = response.byteStream().use { stream ->
                    fs.createTmpFile("aem-").apply {
                        (if (digest != null) DigestOutputStream(outputStream(), digest) else outputStream())
                            .use { stream.copyTo(it) }
                    }
                }

                // rename temporary file based on digest
                val dedup = digest?.let { fs.file("${it.digest().toHexString()}.bin") }
                val output = when {
                    dedup == null -> tmpFile
                    dedup.exists() -> {
                        tmpFile.delete()
                        dedup
                    }
                    tmpFile.renameTo(dedup) -> dedup
                    else -> {
                        Timber.tag(TAG).d("cannot rename tmp file %s to %s", tmpFile, dedup)
                        tmpFile
                    }
                }
                resourceDao.updateLocalFile(resource.uri, response.contentType(), output.name, Date())
                return output
            }
        }

        internal suspend fun removeOrphanedFiles() {
            if (!fs.exists()) return

            // lock the filesystem before removing any orphaned files
            filesystemMutex.write.withLock {
                // determine which files are still being referenced
                val valid = resourceDao.getAll()
                    .mapNotNullTo(mutableSetOf()) { it.getLocalFile(fs) }

                // delete any files not referenced
                withContext(Dispatchers.IO) {
                    fs.rootDir().listFiles()
                        ?.filterNot { it in valid }
                        ?.forEach { it.delete() }
                }
            }
        }
    }
}

private fun ByteArray.toHexString() = joinToString("") { String.format("%02x", it) }

// always round down for simplicity
@VisibleForTesting
internal fun Long.roundTimestamp(interval: Long) = this - this % interval
