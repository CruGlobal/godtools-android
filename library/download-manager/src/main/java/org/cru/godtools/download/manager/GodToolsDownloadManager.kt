package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.google.common.io.CountingInputStream
import dagger.Lazy
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
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
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.download.manager.work.scheduleDownloadTranslationWork
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.model.event.AttachmentUpdateEvent
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
@VisibleForTesting
internal val QUERY_PINNED_TRANSLATIONS = Query.select<Translation>()
    .joins(TranslationTable.SQL_JOIN_LANGUAGE, TranslationTable.SQL_JOIN_TOOL)
    .where(
        LanguageTable.SQL_WHERE_ADDED
            .and(ToolTable.FIELD_ADDED.eq(true))
            .and(TranslationTable.SQL_WHERE_PUBLISHED)
            .and(TranslationTable.FIELD_DOWNLOADED.eq(false))
    )
    .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)

@Singleton
class GodToolsDownloadManager @VisibleForTesting internal constructor(
    private val attachmentsApi: AttachmentsApi,
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val fs: ToolFileSystem,
    private val settings: Settings,
    private val translationsApi: TranslationsApi,
    private val workManager: Lazy<WorkManager>,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    @Inject
    constructor(
        attachmentsApi: AttachmentsApi,
        dao: GodToolsDao,
        eventBus: EventBus,
        fs: ToolFileSystem,
        settings: Settings,
        translationsApi: TranslationsApi,
        workManager: Lazy<WorkManager>
    ) : this(
        attachmentsApi,
        dao,
        eventBus,
        fs,
        settings,
        translationsApi,
        workManager,
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
    }

    suspend fun unpinLanguage(locale: Locale) {
        if (settings.isLanguageProtected(locale)) return
        if (settings.parallelLanguage == locale) settings.parallelLanguage = null

        val language = Language().apply {
            code = locale
            isAdded = false
        }
        withContext(Dispatchers.IO) { dao.update(language, LanguageTable.COLUMN_ADDED) }
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
    internal fun startProgress(translation: TranslationKey) {
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
        if (!fs.exists()) return

        attachmentsMutex.withLock(attachmentId) {
            val attachment = dao.find<Attachment>(attachmentId) ?: return
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
                        dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
                        eventBus.post(AttachmentUpdateEvent)
                    }
                }
            }
        }
    }

    suspend fun importAttachment(attachmentId: Long, data: InputStream) {
        if (!fs.exists()) return

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
    private val pinnedTranslationsJob = coroutineScope.launch {
        dao.getAsFlow(QUERY_PINNED_TRANSLATIONS)
            .collect { it.map { launch { downloadLatestPublishedTranslation(TranslationKey(it)) } }.joinAll() }
    }

    @AnyThread
    fun downloadLatestPublishedTranslationAsync(code: String, locale: Locale) = coroutineScope.launch {
        downloadLatestPublishedTranslation(TranslationKey(code, locale))
    }

    internal suspend fun downloadLatestPublishedTranslation(key: TranslationKey): Boolean {
        require(fs.exists())

        translationsMutex.withLock(key) {
            val translation = dao.getLatestTranslation(key.tool, key.locale, true)?.takeUnless { it.isDownloaded }
                ?: return true

            startProgress(key)
            val downloaded = try {
                val body = translationsApi.download(translation.id).takeIf { it.isSuccessful }?.body()
                if (body != null) {
                    body.byteStream().extractZipFor(translation, body.contentLength())
                    pruneStaleTranslations()
                    true
                } else false
            } catch (e: IOException) {
                false
            } finally {
                finishDownload(key)
            }
            if (!downloaded) workManager.get().scheduleDownloadTranslationWork(key)
            return downloaded
        }
    }

    suspend fun importTranslation(translation: Translation, zipStream: InputStream, size: Long) {
        if (!fs.exists()) return

        val key = TranslationKey(translation)
        translationsMutex.withLock(TranslationKey(translation)) {
            val current = dao.getLatestTranslation(key.tool, key.locale, isPublished = true, isDownloaded = true)
            if (current != null && current.version >= translation.version) return

            startProgress(key)
            try {
                zipStream.extractZipFor(translation, size)
            } finally {
                finishDownload(key)
            }
        }
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
            eventBus.post(TranslationUpdateEvent)
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
        withTimeoutOrNull(CLEANUP_DELAY_INITIAL) { channel.receiveCatching() }
        while (!channel.isClosedForReceive) {
            detectMissingFiles()
            cleanFilesystem()
            withTimeoutOrNull(CLEANUP_DELAY) { channel.receiveCatching() }
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
                Query.select<LocalFile>().get(dao)
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

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal suspend fun shutdown() {
        cleanupActor.close()
        staleAttachmentsJob.cancel()
        toolBannerAttachmentsJob.cancel()
        pinnedTranslationsJob.cancel()
        val job = coroutineScope.coroutineContext[Job]
        if (job is CompletableJob) job.complete()
        job?.join()
        staleAttachmentsJob.join()
        toolBannerAttachmentsJob.join()
        pinnedTranslationsJob.join()
    }

    @WorkerThread
    private suspend fun InputStream.copyTo(localFile: LocalFile) {
        withContext(Dispatchers.IO) { localFile.getFile(fs).outputStream().use { copyTo(it) } }
    }
}
