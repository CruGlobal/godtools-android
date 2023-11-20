package org.cru.godtools.downloadmanager

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.api.TranslationsApi
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.work.scheduleDownloadTranslationWork
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.DownloadedTranslationFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserResult

@VisibleForTesting
internal const val CLEANUP_DELAY = 30_000L

@Singleton
class GodToolsDownloadManager @VisibleForTesting internal constructor(
    private val attachmentsApi: AttachmentsApi,
    private val attachmentsRepository: AttachmentsRepository,
    private val downloadedFilesRepository: DownloadedFilesRepository,
    private val fs: ToolFileSystem,
    private val manifestParser: ManifestParser,
    private val translationsApi: TranslationsApi,
    private val translationsRepository: TranslationsRepository,
    private val workManager: Lazy<WorkManager>,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) {
    @Inject
    internal constructor(
        attachmentsApi: AttachmentsApi,
        attachmentsRepository: AttachmentsRepository,
        downloadedFilesRepository: DownloadedFilesRepository,
        fs: ToolFileSystem,
        manifestParser: ManifestParser,
        translationsApi: TranslationsApi,
        translationsRepository: TranslationsRepository,
        workManager: Lazy<WorkManager>,
    ) : this(
        attachmentsApi,
        attachmentsRepository,
        downloadedFilesRepository,
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
                    val downloadedFile = downloadedFilesRepository.findDownloadedFile(filename)
                    if (attachment.isDownloaded && downloadedFile != null) return
                    attachment.isDownloaded = downloadedFile != null

                    if (downloadedFile == null) {
                        // create a new local file object
                        try {
                            // download attachment
                            attachmentsApi.download(attachmentId).takeIf { it.isSuccessful }?.body()?.byteStream()
                                ?.use {
                                    val file = DownloadedFile(filename)
                                    it.copyTo(file)
                                    downloadedFilesRepository.insertOrIgnore(file)
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
                        val downloadedFile = downloadedFilesRepository.findDownloadedFile(filename)
                        if (attachment.isDownloaded && downloadedFile != null) return@withContext

                        // download the attachment
                        attachment.isDownloaded = false
                        try {
                            // we don't have a local file, so create it
                            if (downloadedFile == null) {
                                val file = DownloadedFile(filename)
                                data.copyTo(file)
                                downloadedFilesRepository.insertOrIgnore(file)
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
    fun downloadLatestPublishedTranslationAsync(code: String, locale: Locale) = coroutineScope.async {
        downloadLatestPublishedTranslation(TranslationKey(code, locale))
    }

    internal suspend fun downloadLatestPublishedTranslation(key: TranslationKey): Boolean {
        require(fs.exists())

        translationsMutex.withLock(key) {
            val translation = translationsRepository.findLatestTranslation(key.tool, key.locale)
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
            val current = translationsRepository.findLatestTranslation(key.tool, key.locale, downloadedOnly = true)
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
                    downloadTranslationFileIfNecessary(it).also {
                        do {
                            val completed = completedFiles.get()
                            updateProgress(key, completed + 1, relatedFiles.size.toLong())
                        } while (!completedFiles.compareAndSet(completed, completed + 1))
                    }
                }
            }.awaitAll().all { it }
        }
        if (!successful) return false

        // record the translation as downloaded
        downloadedFilesRepository.insertOrIgnore(DownloadedTranslationFile(translation, manifestFileName))
        relatedFiles.forEach { downloadedFilesRepository.insertOrIgnore(DownloadedTranslationFile(translation, it)) }
        translationsRepository.markTranslationDownloaded(translation.id, true)

        return true
    }

    private suspend fun downloadTranslationFileIfNecessary(fileName: String): Boolean = filesMutex.withLock(fileName) {
        if (downloadedFilesRepository.findDownloadedFile(fileName) != null) return true
        try {
            val body = translationsApi.downloadFile(fileName).takeIf { it.isSuccessful }?.body() ?: return false
            val downloadedFile = DownloadedFile(fileName)
            withContext(Dispatchers.IO) { body.byteStream().copyTo(downloadedFile) }
            downloadedFilesRepository.insertOrIgnore(downloadedFile)
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
        } else {
            false
        }
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
                            if (downloadedFilesRepository.findDownloadedFile(filename) == null) {
                                val newFile = DownloadedFile(filename)
                                zin.copyTo(newFile)
                                downloadedFilesRepository.insertOrIgnore(newFile)
                            }

                            // associate this file with this translation
                            downloadedFilesRepository.insertOrIgnore(DownloadedTranslationFile(translation, filename))
                        }
                        updateProgress(key, count.count, size)
                    }
                }
            }

            // mark translation as downloaded
            translationsRepository.markTranslationDownloaded(translation.id, true)
        }
    }

    @VisibleForTesting
    internal suspend fun pruneStaleTranslations() = withContext(Dispatchers.Default) {
        // if any translations were updated, send a broadcast
        translationsRepository.markStaleTranslationsAsNotDownloaded()
            .also { if (it) cleanupActor.send(Unit) }
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
            if (fs.exists()) {
                detectMissingFiles()
                deleteOrphanedTranslationFiles()
                deleteUnusedDownloadedFiles()
                deleteOrphanedFiles()
            }
        }
    }

    @VisibleForTesting
    internal suspend fun detectMissingFiles() = filesystemMutex.write.withLock {
        withContext(ioDispatcher) {
            // get the set of all downloaded files
            val files = fs.rootDir().listFiles()?.filterTo(mutableSetOf()) { it.isFile }.orEmpty()

            // check for missing files
            downloadedFilesRepository.getDownloadedFiles()
                .filterNot { it.getFile(fs) in files }
                .forEach { downloadedFilesRepository.delete(it) }
        }
    }

    @VisibleForTesting
    internal suspend fun deleteOrphanedTranslationFiles() = filesystemMutex.write.withLock {
        val downloadedTranslations = translationsRepository.getTranslations()
            .filter { it.isDownloaded }
            .map { it.id }

        coroutineScope {
            downloadedFilesRepository.getDownloadedTranslationFiles()
                .filterNot { it.translationId in downloadedTranslations }
                .forEach { launch { downloadedFilesRepository.delete(it) } }
        }
    }

    @VisibleForTesting
    internal suspend fun deleteUnusedDownloadedFiles() = filesystemMutex.write.withLock {
        withContext(ioDispatcher) {
            val attachments = attachmentsRepository.getAttachments()
                .filter { it.isDownloaded }
                .mapNotNullTo(mutableSetOf()) { it.localFilename }
            val translationFiles = downloadedFilesRepository.getDownloadedTranslationFiles()
                .mapTo(mutableSetOf()) { it.filename }
            downloadedFilesRepository.getDownloadedFiles()
                .filterNot { it.filename in attachments }
                .filterNot { it.filename in translationFiles }
                .forEach {
                    downloadedFilesRepository.delete(it)
                    it.getFile(fs).delete()
                }
        }
    }

    @VisibleForTesting
    internal suspend fun deleteOrphanedFiles() = filesystemMutex.write.withLock {
        withContext(ioDispatcher) {
            fs.rootDir().listFiles()
                ?.filter { downloadedFilesRepository.findDownloadedFile(it.name) == null }
                ?.forEach { it.delete() }
        }
    }
    // endregion Cleanup

    private suspend fun InputStream.copyTo(file: DownloadedFile) = withContext(Dispatchers.IO) {
        file.getFile(fs).outputStream().use { copyTo(it) }
    }

    @Singleton
    internal class Dispatcher @VisibleForTesting internal constructor(
        attachmentsRepository: AttachmentsRepository,
        private val downloadManager: GodToolsDownloadManager,
        downloadedFilesRepository: DownloadedFilesRepository,
        languagesRepository: LanguagesRepository,
        settings: Settings,
        private val toolsRepository: ToolsRepository,
        private val translationsRepository: TranslationsRepository,
        private val coroutineScope: CoroutineScope,
    ) {
        @Inject
        internal constructor(
            attachmentsRepository: AttachmentsRepository,
            downloadManager: GodToolsDownloadManager,
            downloadedFilesRepository: DownloadedFilesRepository,
            languagesRepository: LanguagesRepository,
            settings: Settings,
            toolsRepository: ToolsRepository,
            translationsRepository: TranslationsRepository,
        ) : this(
            attachmentsRepository,
            downloadManager,
            downloadedFilesRepository,
            languagesRepository = languagesRepository,
            settings,
            toolsRepository,
            translationsRepository,
            CoroutineScope(Dispatchers.Default + SupervisorJob())
        )

        init {
            // Download Translations for Favorite Tools in the app language
            settings.appLanguageFlow
                .map { setOf(it) }
                .distinctUntilChanged()
                .downloadFavoriteTranslations()

            // Download Translations for All Tools in the pinned languages
            languagesRepository.getPinnedLanguagesFlow()
                .map { it.mapTo(mutableSetOf()) { it.code } }
                .distinctUntilChanged()
                .downloadAllToolTranslations()

            // Stale Downloaded Attachments
            attachmentsRepository.getAttachmentsFlow()
                .combine(downloadedFilesRepository.getDownloadedFilesFlow()) { attachments, files ->
                    val filenames = files.mapTo(mutableSetOf()) { it.filename }
                    attachments
                        .filter { it.isDownloaded && it.localFilename !in filenames }
                        .mapTo(mutableSetOf()) { it.id }
                }
                .downloadAttachments()

            // Tool Banner Attachments
            attachmentsRepository.getAttachmentsFlow()
                .combine(toolsRepository.getAllToolsFlow()) { attachments, tools ->
                    val banners = tools.flatMapTo(mutableSetOf()) {
                        setOfNotNull(it.bannerId, it.detailsBannerId, it.detailsBannerAnimationId)
                    }

                    attachments
                        .filter { it.id in banners && !it.isDownloaded }
                        .mapTo(mutableSetOf()) { it.id }
                }
                .downloadAttachments()
        }

        @VisibleForTesting
        internal val downloadTranslationsForDefaultLanguageJob =
            flowOf(setOf(Settings.defaultLanguage)).downloadFavoriteTranslations()

        private fun Flow<Collection<Locale>>.downloadFavoriteTranslations() = toolsRepository.getFavoriteToolsFlow()
            .downloadTranslations(this)

        private fun Flow<Collection<Locale>>.downloadAllToolTranslations() = toolsRepository.getAllToolsFlow()
            .downloadTranslations(this)

        private fun Flow<Set<Long>>.downloadAttachments() = this
            .distinctUntilChanged()
            .conflate()
            .onEach { it.forEach { coroutineScope.launch { downloadManager.downloadAttachment(it) } } }
            .launchIn(coroutineScope)

        private fun Flow<Collection<Tool>>.downloadTranslations(languages: Flow<Collection<Locale>>) = this
            .map { it.mapNotNullTo(mutableSetOf()) { it.code } }
            .distinctUntilChanged()
            .combineTransformLatest(languages) { t, l ->
                emitAll(translationsRepository.getTranslationsForToolsAndLocalesFlow(t, l))
            }
            .map {
                it.filterNot { it.isDownloaded }
                    .map { TranslationKey(it) }
                    .toSet()
            }
            .distinctUntilChanged()
            .conflate()
            .onEach {
                it.forEach { coroutineScope.launch { downloadManager.downloadLatestPublishedTranslation(it) } }
            }
            .launchIn(coroutineScope)
    }
}
