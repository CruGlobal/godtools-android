package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.work.WorkManager
import com.google.common.io.CountingInputStream
import dagger.Lazy
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.api.TranslationsApi
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.download.manager.db.DownloadManagerRepository
import org.cru.godtools.download.manager.work.scheduleDownloadTranslationWork
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserResult
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository

@VisibleForTesting
internal const val CLEANUP_DELAY = 30_000L

@VisibleForTesting
internal val QUERY_LOCAL_FILES = Query.select<LocalFile>()

@VisibleForTesting
internal val QUERY_STALE_TRANSLATIONS = Query.select<Translation>()
    .where(TranslationTable.SQL_WHERE_DOWNLOADED)
    .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)

@VisibleForTesting
internal val QUERY_CLEAN_ORPHANED_TRANSLATION_FILES = Query.select<TranslationFile>()
    .join(
        TranslationFileTable.SQL_JOIN_TRANSLATION.type("LEFT")
            .andOn(TranslationTable.SQL_WHERE_DOWNLOADED)
    )
    .where(TranslationTable.FIELD_ID.`is`(Expression.NULL))
@VisibleForTesting
internal val QUERY_CLEAN_ORPHANED_LOCAL_FILES = Query.select<LocalFile>()
    .join(LocalFileTable.SQL_JOIN_TRANSLATION_FILE.type("LEFT"))
    .where(TranslationFileTable.FIELD_FILE.`is`(Expression.NULL))

@Singleton
class GodToolsDownloadManager @VisibleForTesting internal constructor(
    private val attachmentsApi: AttachmentsApi,
    private val attachmentsRepository: AttachmentsRepository,
    private val dao: GodToolsDao,
    private val fs: ToolFileSystem,
    private val manifestParser: ManifestParser,
    private val translationsApi: TranslationsApi,
    private val translationsRepository: TranslationsRepository,
    private val workManager: Lazy<WorkManager>,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    @Inject
    internal constructor(
        attachmentsApi: AttachmentsApi,
        attachmentsRepository: AttachmentsRepository,
        dao: GodToolsDao,
        fs: ToolFileSystem,
        manifestParser: ManifestParser,
        translationsApi: TranslationsApi,
        translationsRepository: TranslationsRepository,
        workManager: Lazy<WorkManager>
    ) : this(
        attachmentsApi,
        attachmentsRepository,
        dao,
        fs,
        manifestParser,
        translationsApi,
        translationsRepository,
        workManager,
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    private val attachmentsMutex = MutexMap()
    private val filesystemMutex = ReadWriteMutex()
    private val filesMutex = MutexMap()
    private val translationsMutex = MutexMap()

    // region Download Progress
    private val downloadProgressStateFlows = mutableMapOf<TranslationKey, MutableStateFlow<DownloadProgress?>>()

    @AnyThread
    private fun getDownloadProgressStateFlow(translation: TranslationKey) = synchronized(downloadProgressStateFlows) {
        downloadProgressStateFlows.getOrPut(translation) { MutableStateFlow(null) }
    }

    @AnyThread
    fun getDownloadProgressFlow(tool: String, locale: Locale): Flow<DownloadProgress?> =
        getDownloadProgressStateFlow(TranslationKey(tool, locale))

    @AnyThread
    @VisibleForTesting
    internal fun startProgress(translation: TranslationKey) {
        getDownloadProgressStateFlow(translation).compareAndSet(null, DownloadProgress.INITIAL)
    }

    @AnyThread
    @VisibleForTesting
    internal fun updateProgress(translation: TranslationKey, progress: Long, max: Long) {
        getDownloadProgressStateFlow(translation).value = DownloadProgress(progress, max)
    }

    @AnyThread
    @VisibleForTesting
    internal fun finishDownload(translation: TranslationKey) {
        getDownloadProgressStateFlow(translation).value = null
    }
    // endregion Download Progress

    // region Attachments
    @VisibleForTesting
    internal suspend fun downloadAttachment(attachmentId: Long) {
        if (!fs.exists()) return

        attachmentsMutex.withLock(attachmentId) {
            val attachment = attachmentsRepository.findAttachment(attachmentId) ?: return
            val filename = attachment.localFilename ?: return
            val wasDownloaded = attachment.isDownloaded

            filesystemMutex.read.withLock {
                filesMutex.withLock(filename) {
                    val localFile = dao.find<LocalFile>(filename)
                    if (attachment.isDownloaded && localFile != null) return
                    attachment.isDownloaded = localFile != null

                    if (localFile == null) {
                        // create a new local file object
                        try {
                            // download attachment
                            attachmentsApi.download(attachmentId).takeIf { it.isSuccessful }?.body()?.byteStream()
                                ?.use {
                                    val file = LocalFile(filename)
                                    it.copyTo(file)
                                    dao.updateOrInsert(file)
                                    attachment.isDownloaded = true
                                }
                        } catch (ignored: IOException) {
                        }
                    }

                    if (attachment.isDownloaded || wasDownloaded) {
                        attachmentsRepository.updateAttachmentDownloaded(attachmentId, attachment.isDownloaded)
                    }
                }
            }
        }
    }

    suspend fun importAttachment(attachmentId: Long, data: InputStream) {
        if (!fs.exists()) return

        attachmentsMutex.withLock(attachmentId) {
            val attachment = attachmentsRepository.findAttachment(attachmentId) ?: return
            val filename = attachment.localFilename ?: return

            filesystemMutex.read.withLock {
                filesMutex.withLock(filename) {
                    withContext(Dispatchers.IO) {
                        // short-circuit if the attachment is already downloaded
                        val localFile: LocalFile? = dao.find(filename)
                        if (attachment.isDownloaded && localFile != null) return@withContext

                        // download the attachment
                        attachment.isDownloaded = false
                        try {
                            // we don't have a local file, so create it
                            if (localFile == null) {
                                val file = LocalFile(filename)
                                data.copyTo(file)
                                dao.updateOrInsert(file)
                            }

                            // mark attachment as downloaded
                            attachment.isDownloaded = true
                        } finally {
                            // update attachment download state
                            attachmentsRepository.updateAttachmentDownloaded(attachmentId, attachment.isDownloaded)
                        }
                    }
                }
            }
        }
    }
    // endregion Attachments

    // region Translations
    @AnyThread
    fun downloadLatestPublishedTranslationAsync(code: String, locale: Locale) = coroutineScope.launch {
        downloadLatestPublishedTranslation(TranslationKey(code, locale))
    }

    internal suspend fun downloadLatestPublishedTranslation(key: TranslationKey): Boolean {
        require(fs.exists())

        translationsMutex.withLock(key) {
            val translation = translationsRepository.getLatestTranslation(key.tool, key.locale)
                ?.takeUnless { it.isDownloaded }
                ?: return true

            startProgress(key)
            val downloaded = try {
                downloadTranslationFiles(translation) || downloadTranslationZip(translation)
            } finally {
                finishDownload(key)
            }
            if (downloaded) pruneStaleTranslations()
            if (!downloaded) workManager.get().scheduleDownloadTranslationWork(key)
            return downloaded
        }
    }

    suspend fun importTranslation(translation: Translation, zipStream: InputStream, size: Long) {
        if (!fs.exists()) return

        val key = TranslationKey(translation)
        translationsMutex.withLock(TranslationKey(translation)) {
            val current = translationsRepository.getLatestTranslation(key.tool, key.locale, isDownloaded = true)
            if (current != null && current.version >= translation.version) return

            startProgress(key)
            try {
                zipStream.extractZipFor(translation, size)
            } finally {
                finishDownload(key)
            }
        }
    }

    private suspend fun downloadTranslationFiles(translation: Translation): Boolean = filesystemMutex.read.withLock {
        // download manifest if necessary
        val manifestFileName = translation.manifestFileName ?: return false
        if (!downloadTranslationFileIfNecessary(manifestFileName)) return false

        // parse manifest
        val parserResult = manifestParser.parseManifest(
            manifestFileName,
            manifestParser.defaultConfig.withParseRelated(false)
        )
        val manifest = (parserResult as? ParserResult.Data)?.manifest ?: return false

        // download all files the manifest references
        val key = TranslationKey(translation)
        val relatedFiles = manifest.relatedFiles
        val completedFiles = AtomicLong(0)
        val successful = coroutineScope {
            relatedFiles.map {
                async {
                    val resp = downloadTranslationFileIfNecessary(it)
                    do {
                        val completed = completedFiles.get()
                        updateProgress(key, completed + 1, relatedFiles.size.toLong())
                    } while (!completedFiles.compareAndSet(completed, completed + 1))
                    resp
                }
            }.awaitAll().all { it }
        }
        if (!successful) return false

        // record the translation as downloaded
        translation.isDownloaded = true
        dao.transaction {
            dao.updateOrInsert(TranslationFile(translation, manifestFileName))
            relatedFiles.forEach { dao.updateOrInsert(TranslationFile(translation, it)) }
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
        }

        return true
    }

    private suspend fun downloadTranslationFileIfNecessary(fileName: String): Boolean = filesMutex.withLock(fileName) {
        if (dao.find<LocalFile>(fileName) != null) return true
        try {
            val body = translationsApi.downloadFile(fileName).takeIf { it.isSuccessful }?.body() ?: return false
            val localFile = LocalFile(fileName)
            withContext(ioDispatcher) { body.byteStream().copyTo(localFile) }
            dao.updateOrInsert(localFile)
            return true
        } catch (e: IOException) {
            return false
        }
    }

    private suspend fun downloadTranslationZip(translation: Translation) = try {
        val body = translationsApi.download(translation.id).takeIf { it.isSuccessful }?.body()
        if (body != null) {
            body.byteStream().extractZipFor(translation, body.contentLength())
            true
        } else false
    } catch (e: IOException) {
        false
    }

    @GuardedBy("translationsMutex")
    private suspend fun InputStream.extractZipFor(translation: Translation, size: Long) {
        val key = TranslationKey(translation)
        filesystemMutex.read.withLock {
            val count = CountingInputStream(this)
            withContext(Dispatchers.IO) {
                ZipInputStream(count.buffered()).use { zin ->
                    while (true) {
                        val ze = zin.nextEntry ?: break
                        val filename = ze.name
                        filesMutex.withLock(filename) {
                            // write the file if it hasn't been downloaded before
                            if (dao.find<LocalFile>(filename) == null) {
                                val newFile = LocalFile(filename)
                                zin.copyTo(newFile)
                                dao.updateOrInsert(newFile)
                            }

                            // associate this file with this translation
                            dao.updateOrInsert(TranslationFile(translation, filename))
                        }
                        updateProgress(key, count.count, size)
                    }
                }
            }

            // mark translation as downloaded
            translation.isDownloaded = true
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
        }
    }

    @VisibleForTesting
    internal suspend fun pruneStaleTranslations() = withContext(Dispatchers.Default) {
        val changes = dao.transaction(true) {
            val seen = mutableSetOf<TranslationKey>()
            dao.get(QUERY_STALE_TRANSLATIONS).asSequence()
                .filterNot { seen.add(TranslationKey(it)) }
                .filter { it.isDownloaded }
                .onEach {
                    it.isDownloaded = false
                    dao.update(it, TranslationTable.COLUMN_DOWNLOADED)
                }
                .count()
        }

        // if any translations were updated, send a broadcast
        if (changes > 0) cleanupActor.send(Unit)
    }
    // endregion Translations

    // region Cleanup
    @VisibleForTesting
    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    internal val cleanupActor = coroutineScope.actor<Unit>(capacity = Channel.CONFLATED) {
        consumeAsFlow().onStart { emit(Unit) }.transformLatest {
            delay(CLEANUP_DELAY)
            emit(Unit)
        }.conflate().collect {
            detectMissingFiles()
            cleanFilesystem()
        }
    }

    @VisibleForTesting
    internal suspend fun detectMissingFiles() {
        if (!fs.exists()) return

        filesystemMutex.write.withLock {
            withContext(ioDispatcher) {
                // get the set of all downloaded files
                val files = fs.rootDir().listFiles()?.filterTo(mutableSetOf()) { it.isFile }.orEmpty()

                // check for missing files
                dao.get(QUERY_LOCAL_FILES)
                    .filterNot { files.contains(it.getFile(fs)) }
                    .forEach { dao.delete(it) }
            }
        }
    }

    @VisibleForTesting
    internal suspend fun cleanFilesystem() {
        if (!fs.exists()) return
        filesystemMutex.write.withLock {
            withContext(ioDispatcher) {
                // remove any TranslationFiles for translations that are no longer downloaded
                dao.get(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES).forEach { dao.delete(it) }

                // delete any LocalFiles that are no longer being used
                val attachments = attachmentsRepository.getAttachments()
                    .filter { it.isDownloaded }
                    .mapNotNullTo(mutableSetOf()) { it.localFilename }
                dao.get(QUERY_CLEAN_ORPHANED_LOCAL_FILES)
                    .filterNot { it.filename in attachments }
                    .forEach {
                        dao.delete(it)
                        it.getFile(fs).delete()
                    }

                // delete any orphaned files
                fs.rootDir().listFiles()
                    ?.filter { dao.find<LocalFile>(it.name) == null }
                    ?.forEach { it.delete() }
            }
        }
    }
    // endregion Cleanup

    @WorkerThread
    private suspend fun InputStream.copyTo(localFile: LocalFile) {
        withContext(Dispatchers.IO) { localFile.getFile(fs).outputStream().use { copyTo(it) } }
    }

    @Singleton
    @OptIn(ExperimentalCoroutinesApi::class)
    internal class Dispatcher @VisibleForTesting internal constructor(
        attachmentsRepository: AttachmentsRepository,
        dao: GodToolsDao,
        private val downloadManager: GodToolsDownloadManager,
        repository: DownloadManagerRepository,
        settings: Settings,
        coroutineScope: CoroutineScope
    ) {
        @Inject
        internal constructor(
            attachmentsRepository: AttachmentsRepository,
            dao: GodToolsDao,
            downloadManager: GodToolsDownloadManager,
            repository: DownloadManagerRepository,
            settings: Settings
        ) : this(
            attachmentsRepository,
            dao,
            downloadManager,
            repository,
            settings,
            CoroutineScope(Dispatchers.Default + SupervisorJob())
        )

        init {
            // Download Favorite tool translations in the primary, parallel, and default languages
            settings.primaryLanguageFlow
                .combineTransform(settings.parallelLanguageFlow) { prim, para ->
                    emit(listOfNotNull(prim, para, Settings.defaultLanguage))
                }
                .flatMapLatest { repository.getFavoriteTranslationsThatNeedDownload(it) }
                .map { it.map { TranslationKey(it) }.toSet() }
                .distinctUntilChanged()
                .conflate()
                .onEach {
                    coroutineScope { it.forEach { launch { downloadManager.downloadLatestPublishedTranslation(it) } } }
                }
                .launchIn(coroutineScope)

            // Stale Downloaded Attachments
            attachmentsRepository.getAttachmentsFlow()
                .combineTransform(dao.getAsFlow(Query.select<LocalFile>())) { attachments, files ->
                    val filenames = files.mapTo(mutableSetOf()) { it.filename }
                    emit(attachments.filter { it.isDownloaded && it.localFilename !in filenames }.map { it.id }.toSet())
                }
                .distinctUntilChanged()
                .conflate()
                .onEach { coroutineScope { it.forEach { launch { downloadManager.downloadAttachment(it) } } } }
                .launchIn(coroutineScope)

            // Tool Banner Attachments
            attachmentsRepository.getAttachmentsFlow()
                .combineTransform(dao.getAsFlow(Query.select<Tool>())) { attachments, tools ->
                    val banners =
                        tools.flatMap { listOfNotNull(it.bannerId, it.detailsBannerId, it.detailsBannerAnimationId) }
                    emit(attachments.filter { !it.isDownloaded && it.id in banners }.map { it.id }.toSet())
                }
                .distinctUntilChanged()
                .conflate()
                .onEach { coroutineScope { it.forEach { launch { downloadManager.downloadAttachment(it) } } } }
                .launchIn(coroutineScope)
        }
    }
}
