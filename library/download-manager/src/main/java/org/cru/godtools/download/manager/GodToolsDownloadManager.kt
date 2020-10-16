package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.io.CountingInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.api.TranslationsApi
import org.cru.godtools.base.FileManager
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.LanguageUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TAG = "GodToolsDownloadManager"

@VisibleForTesting
internal const val CLEANUP_DELAY = HOUR_IN_MS
@VisibleForTesting
internal const val CLEANUP_DELAY_INITIAL = MIN_IN_MS

@VisibleForTesting
internal val QUERY_STALE_ATTACHMENTS = Query.select<Attachment>()
    .distinct(true)
    .join(AttachmentTable.SQL_JOIN_LOCAL_FILE.type("LEFT"))
    .where(AttachmentTable.SQL_WHERE_DOWNLOADED.and(LocalFileTable.FIELD_NAME.isNull()))
@VisibleForTesting
internal val QUERY_TOOL_BANNER_ATTACHMENTS = Query.select<Attachment>()
    .distinct(true)
    .join(
        AttachmentTable.SQL_JOIN_TOOL.andOn(
            ToolTable.FIELD_DETAILS_BANNER.eq(AttachmentTable.FIELD_ID)
                .or(ToolTable.FIELD_BANNER.eq(AttachmentTable.FIELD_ID))
        )
    )
    .where(AttachmentTable.FIELD_DOWNLOADED.eq(false))

open class KotlinGodToolsDownloadManager @VisibleForTesting internal constructor(
    private val attachmentsApi: AttachmentsApi,
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val fileManager: FileManager,
    private val settings: Settings,
    private val translationsApi: TranslationsApi,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    constructor(
        attachmentsApi: AttachmentsApi,
        dao: GodToolsDao,
        eventBus: EventBus,
        fileManager: FileManager,
        settings: Settings,
        translationsApi: TranslationsApi
    ) : this(
        attachmentsApi,
        dao,
        eventBus,
        fileManager,
        settings,
        translationsApi,
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    private val attachmentsMutex = MutexMap()
    private val filesystemMutex = ReadWriteMutex()
    private val filesMutex = MutexMap()
    private val translationsMutex = MutexMap()

    // region Tool/Language pinning
    @AnyThread
    fun pinToolAsync(code: String) = coroutineScope.launch { pinTool(code) }
    suspend fun pinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = true
        }
        withContext(Dispatchers.IO) { dao.update(tool, ToolTable.COLUMN_ADDED) }
        eventBus.post(ToolUpdateEvent)
    }

    @AnyThread
    fun unpinToolAsync(code: String) = coroutineScope.launch { unpinTool(code) }
    suspend fun unpinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = false
        }
        withContext(Dispatchers.IO) { dao.update(tool, ToolTable.COLUMN_ADDED) }
        eventBus.post(ToolUpdateEvent)
    }

    @AnyThread
    fun pinLanguageAsync(locale: Locale) = coroutineScope.launch { pinLanguage(locale) }
    suspend fun pinLanguage(locale: Locale) {
        val language = Language().apply {
            code = locale
            isAdded = true
        }
        withContext(Dispatchers.IO) { dao.update(language, LanguageTable.COLUMN_ADDED) }
        eventBus.post(LanguageUpdateEvent)
    }

    suspend fun unpinLanguage(locale: Locale) {
        if (settings.isLanguageProtected(locale)) return
        if (settings.parallelLanguage == locale) settings.parallelLanguage = null

        val language = Language().apply {
            code = locale
            isAdded = false
        }
        withContext(Dispatchers.IO) { dao.update(language, LanguageTable.COLUMN_ADDED) }
        eventBus.post(LanguageUpdateEvent)
    }
    // endregion Tool/Language pinning

    // region Download Progress
    private val downloadProgressLiveData = mutableMapOf<TranslationKey, MutableLiveData<DownloadProgress?>>()

    @AnyThread
    private fun getDownloadProgressLiveData(translation: TranslationKey) = synchronized(downloadProgressLiveData) {
        downloadProgressLiveData.getOrPut(translation) { DownloadProgressLiveData() }
    }

    @MainThread
    fun getDownloadProgressLiveData(tool: String, locale: Locale): LiveData<DownloadProgress?> =
        getDownloadProgressLiveData(TranslationKey(tool, locale))

    @AnyThread
    @VisibleForTesting
    fun startProgress(translation: TranslationKey) {
        getDownloadProgressLiveData(translation).postValue(DownloadProgress.INITIAL)
    }

    @AnyThread
    @VisibleForTesting
    internal fun updateProgress(translation: TranslationKey, progress: Long, max: Long) {
        getDownloadProgressLiveData(translation).postValue(DownloadProgress(progress, max))
    }

    @AnyThread
    @VisibleForTesting
    internal fun finishDownload(translation: TranslationKey) {
        getDownloadProgressLiveData(translation).postValue(null)
    }
    // endregion Download Progress

    // region Attachments
    private val staleAttachmentsJob = coroutineScope.launch {
        dao.getAsFlow(QUERY_STALE_ATTACHMENTS).collect { it.map { launch { downloadAttachment(it.id) } }.joinAll() }
    }
    private val toolBannerAttachmentsJob = coroutineScope.launch {
        dao.getAsFlow(QUERY_TOOL_BANNER_ATTACHMENTS)
            .collect { it.map { launch { downloadAttachment(it.id) } }.joinAll() }
    }

    @VisibleForTesting
    internal suspend fun downloadAttachment(attachmentId: Long) {
        if (!fileManager.createResourcesDir()) return

        attachmentsMutex.withLock(attachmentId) {
            val attachment: Attachment = dao.find(attachmentId) ?: return
            val filename = attachment.localFilename ?: return

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

                    dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
                    eventBus.post(AttachmentUpdateEvent)
                }
            }
        }
    }

    suspend fun importAttachment(attachmentId: Long, data: InputStream) {
        if (!fileManager.createResourcesDir()) return

        attachmentsMutex.withLock(attachmentId) {
            val attachment: Attachment = dao.find(attachmentId) ?: return
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
                            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
                            eventBus.post(AttachmentUpdateEvent)
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

    @WorkerThread
    fun downloadLatestPublishedTranslation(key: TranslationKey) {
        if (runBlocking { !fileManager.createResourcesDir() }) return

        runBlocking {
            translationsMutex.withLock(key) {
                dao.getLatestTranslation(key.tool, key.locale, true)?.takeUnless { it.isDownloaded }?.let { trans ->
                    startProgress(key)
                    try {
                        translationsApi.download(trans.id).takeIf { it.isSuccessful }?.body()?.let { body ->
                            body.byteStream().extractZipFor(trans, body.contentLength())
                            pruneStaleTranslations()
                        }
                    } catch (ignored: IOException) {
                    }
                }

                // We always finish the download (even if we didn't start it) because of the following race condition:
                //
                // [1] enqueuePendingPublishedTranslations() loads the list of pending downloads from the database
                // [2] downloadLatestPublishedTranslation() finishes downloading one of the translations loaded by [1]
                // [1] enqueuePendingPublishedTranslations() triggers startProgress() on already downloaded translation
                // [1] downloadLatestPublishedTranslation() short-circuits on the actual download logic
                // [1] we still need to call finishDownload()
                finishDownload(key)
            }
        }
    }

    suspend fun importTranslation(translation: Translation, zipStream: InputStream, size: Long) {
        if (!fileManager.createResourcesDir()) return

        val key = TranslationKey(translation)
        translationsMutex.withLock(TranslationKey(translation)) {
            val current = dao.getLatestTranslation(key.tool, key.locale, isPublished = true, isDownloaded = true)
            if (current != null && current.version >= translation.version) return

            zipStream.extractZipFor(translation, size)
        }
    }

    @GuardedBy("translationsMutex")
    private suspend fun InputStream.extractZipFor(translation: Translation, size: Long) {
        val key = TranslationKey(translation)
        try {
            startProgress(key)
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
                eventBus.post(TranslationUpdateEvent)
            }
        } finally {
            finishDownload(key)
        }
    }

    @VisibleForTesting
    internal suspend fun pruneStaleTranslations() = withContext(Dispatchers.Default) {
        val changes = dao.transaction(true) {
            val seen = mutableSetOf<TranslationKey>()
            Query.select<Translation>()
                .where(TranslationTable.SQL_WHERE_DOWNLOADED)
                .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
                .get(dao).asSequence()
                .filterNot { seen.add(TranslationKey(it)) }
                .filter { it.isDownloaded }
                .onEach {
                    it.isDownloaded = false
                    dao.update(it, TranslationTable.COLUMN_DOWNLOADED)
                }
                .count()
        }

        // if any translations were updated, send a broadcast
        if (changes > 0) {
            eventBus.post(TranslationUpdateEvent)
            cleanupActor.send(RunCleanup)
        }
    }
    // endregion Translations

    // region Cleanup
    @VisibleForTesting
    internal object RunCleanup

    @VisibleForTesting
    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    internal val cleanupActor = coroutineScope.actor<RunCleanup>(capacity = Channel.CONFLATED) {
        withTimeoutOrNull(CLEANUP_DELAY_INITIAL) { channel.receiveOrNull() }
        while (!channel.isClosedForReceive) {
            detectMissingFiles()
            cleanFilesystem()
            withTimeoutOrNull(CLEANUP_DELAY) { channel.receiveOrNull() }
        }
    }

    @VisibleForTesting
    internal suspend fun detectMissingFiles() {
        if (!fileManager.createResourcesDir()) return

        filesystemMutex.write.withLock {
            withContext(ioDispatcher) {
                // get the set of all downloaded files
                val files = fileManager.getResourcesDir().listFiles()?.filterTo(mutableSetOf()) { it.isFile }.orEmpty()

                // check for missing files
                Query.select<LocalFile>().get(dao)
                    .filterNot { files.contains(it.getFile(fileManager)) }
                    .forEach { dao.delete(it) }
            }
        }
    }

    @VisibleForTesting
    internal suspend fun cleanFilesystem() {
        if (!fileManager.createResourcesDir()) return
        filesystemMutex.write.withLock {
            withContext(ioDispatcher) {
                // remove any TranslationFiles for translations that are no longer downloaded
                Query.select<TranslationFile>()
                    .join(
                        TranslationFileTable.SQL_JOIN_TRANSLATION.type("LEFT")
                            .andOn(TranslationTable.SQL_WHERE_DOWNLOADED)
                    )
                    .where(TranslationTable.FIELD_ID.`is`(Expression.NULL))
                    .get(dao).forEach { dao.delete(it) }

                // delete any LocalFiles that are no longer being used
                Query.select<LocalFile>()
                    .join(LocalFileTable.SQL_JOIN_ATTACHMENT.type("LEFT"))
                    .join(LocalFileTable.SQL_JOIN_TRANSLATION_FILE.type("LEFT"))
                    .where(
                        AttachmentTable.FIELD_ID.`is`(Expression.NULL)
                            .and(TranslationFileTable.FIELD_FILE.`is`(Expression.NULL))
                    )
                    .get(dao)
                    .forEach {
                        dao.delete(it)
                        it.getFile(fileManager)?.delete()
                    }

                // delete any orphaned files
                fileManager.getResourcesDir().listFiles()
                    ?.filter { dao.find<LocalFile>(it.name) == null }
                    ?.forEach { it.delete() }
            }
        }
    }
    // endregion Cleanup

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal fun shutdown() {
        cleanupActor.close()
        runBlocking {
            staleAttachmentsJob.cancel()
            toolBannerAttachmentsJob.cancel()
            val job = coroutineScope.coroutineContext[Job]
            if (job is CompletableJob) job.complete()
            job?.join()
            staleAttachmentsJob.join()
            toolBannerAttachmentsJob.join()
        }
    }

    @WorkerThread
    private suspend fun InputStream.copyTo(localFile: LocalFile) {
        val file = localFile.getFile(fileManager)
            ?: throw FileNotFoundException("${localFile.filename} (File could not be created)")

        withContext(Dispatchers.IO) { file.outputStream().use { copyTo(it) } }
    }
}
