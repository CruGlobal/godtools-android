package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.room.InvalidationTracker
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection.HTTP_OK
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.guava.asListenableFuture
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.support.extractResources
import org.cru.godtools.article.aem.service.support.findAemArticles
import org.cru.godtools.article.aem.util.AemFileManager
import org.cru.godtools.article.aem.util.addExtension
import org.cru.godtools.xml.model.Manifest
import timber.log.Timber

private const val TAG = "AemArticleManager"

@VisibleForTesting
internal const val CLEANUP_DELAY = HOUR_IN_MS
@VisibleForTesting
internal const val CLEANUP_DELAY_INITIAL = MIN_IN_MS
private const val CACHE_BUSTING_INTERVAL_JSON = HOUR_IN_MS

open class KotlinAemArticleManager @JvmOverloads constructor(
    private val aemDb: ArticleRoomDatabase,
    private val api: AemApi,
    private val fileManager: AemFileManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val aemImportMutex = MutexMap()
    private val articleMutex = MutexMap()
    private val filesystemMutex = ReadWriteMutex()
    private val resourceMutex = MutexMap()

    // region Deeplinked Article
    @AnyThread
    suspend fun downloadDeeplinkedArticle(uri: Uri) {
        aemDb.aemImportRepository().accessAemImport(AemImport(uri).apply { lastAccessed = Date() })
        syncAemImport(uri, false)
        downloadArticle(uri, false)
    }
    // endregion Deeplinked Article

    // region AEM Import
    @Deprecated("Use the coroutines method instead of wrapping it in a ListenableFuture")
    @AnyThread
    protected fun enqueueSyncAemImport(uri: Uri, force: Boolean) = coroutineScope.async {
        syncAemImport(uri, force)
        true
    }.asListenableFuture()

    @Deprecated("Use the coroutines method instead of wrapping it in a ListenableFuture")
    protected fun syncAemImportsFromManifestAsync(manifest: Manifest?, force: Boolean) =
        coroutineScope.async { syncAemImportsFromManifest(manifest, force) }.asListenableFuture()

    @AnyThread
    suspend fun syncAemImportsFromManifest(manifest: Manifest?, force: Boolean) = coroutineScope {
        manifest?.aemImports?.forEach { launch { syncAemImport(it, force) } }
    }

    /**
     * This method is responsible for syncing an individual AEM Import url to the AEM Article database.
     *
     * @param baseUri The base AEM Import URL to sync
     * @param force   This flag indicates that this sync should ignore the lastUpdated time
     */
    @WorkerThread
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

    init {
        aemDb.aemImportDao().all
            .filter { it.isStale() }
            .forEach { coroutineScope.launch { syncAemImport(it.uri, false) } }
    }
    // endregion AEM Import

    // region Download Article
    @Deprecated("Use the coroutines method instead of wrapping it in a ListenableFuture")
    @AnyThread
    protected fun downloadArticleAsync(uri: Uri, force: Boolean) = coroutineScope.async {
        downloadArticle(uri, force)
        true
    }.asListenableFuture()

    @WorkerThread
    suspend fun downloadArticle(uri: Uri, force: Boolean) {
        articleMutex.withLock(uri) {
            // short-circuit if there isn't an Article for the specified Uri or if the article doesn't need to be downloaded
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
    @WorkerThread
    protected fun Collection<Resource>.downloadResourcesNeedingUpdate() {
        filter { it.needsDownload() }.forEach { coroutineScope.launch { downloadResource(it.uri, false) } }
    }

    @WorkerThread
    suspend fun downloadResource(uri: Uri, force: Boolean) {
        val resourceDao = aemDb.resourceDao()

        resourceMutex.withLock(uri) {
            // short-circuit if the resource doesn't exist or it doesn't need to be downloaded
            val resource = resourceDao.find(uri)
            if (resource == null || (!force && !resource.needsDownload())) return

            // download the resource
            try {
                api.downloadResource(uri).takeIf { it.code() == HTTP_OK }?.body()?.let { response ->
                    response.byteStream().use {
                        it.writeToDisk { file ->
                            resourceDao.updateLocalFile(uri, response.contentType(), file.name, Date())
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.tag(TAG).d(e, "Error downloading attachment %s", uri)
            }
        }
    }

    @VisibleForTesting
    @Throws(IOException::class)
    internal suspend fun InputStream.writeToDisk(onSuccess: (File) -> Unit): File? {
        if (!fileManager.createDir()) return null

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
            val tmpFile = fileManager.createTmpFile("aem-").apply {
                (if (digest != null) DigestOutputStream(outputStream(), digest) else outputStream())
                    .use { copyTo(it) }
            }

            // rename temporary file based on digest
            val dedup = digest?.let { fileManager.getFile("${it.digest().toHexString()}.bin") }
            return when {
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
            }.also(onSuccess)
        }
    }
    // endregion Download Resource

    // region Cleanup
    private object RunCleanup

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    private val cleanupActor = coroutineScope.actor<RunCleanup>(capacity = Channel.CONFLATED) {
        withTimeoutOrNull(CLEANUP_DELAY_INITIAL) { channel.receiveOrNull() }
        while (!channel.isClosedForReceive) {
            cleanOrphanedFiles()
            withTimeoutOrNull(CLEANUP_DELAY) { channel.receiveOrNull() }
        }
    }

    @WorkerThread
    private suspend fun cleanOrphanedFiles() {
        if (!fileManager.createDir()) return

        // lock the filesystem before removing any orphaned files
        filesystemMutex.write.withLock {
            // determine which files are still being referenced
            val valid = aemDb.resourceDao().getAll()
                .mapNotNullTo(mutableSetOf()) { it.getLocalFile(fileManager) }

            // delete any files not referenced
            fileManager.getDir().listFiles()
                ?.filterNot { it in valid }
                ?.forEach { it.delete() }
        }
    }

    init {
        aemDb.invalidationTracker.addObserver(object : InvalidationTracker.Observer(Resource.TABLE_NAME) {
            override fun onInvalidated(tables: Set<String>) {
                if (Resource.TABLE_NAME in tables) cleanupActor.offer(RunCleanup)
            }
        })
    }
    // endregion Cleanup

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal suspend fun shutdown() {
        cleanupActor.close()
        val job = coroutineScope.coroutineContext[Job]
        if (job is CompletableJob) job.complete()
        job?.join()
    }
}

private fun ByteArray.toHexString() = joinToString("") { String.format("%02x", it) }

// always round down for simplicity
@VisibleForTesting
internal fun Long.roundTimestamp(interval: Long) = this - this % interval
