package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.api.TranslationsApi
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.nullableArgumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsDownloadManagerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val resourcesDir = File.createTempFile("resources", "").also {
        it.delete()
        it.mkdirs()
    }

    private lateinit var attachmentsApi: AttachmentsApi
    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus
    private lateinit var fs: ToolFileSystem
    private lateinit var settings: Settings
    private lateinit var translationsApi: TranslationsApi
    private lateinit var workManager: WorkManager
    private lateinit var testScope: TestCoroutineScope

    private lateinit var downloadManager: GodToolsDownloadManager

    private lateinit var observer: Observer<DownloadProgress?>

    private val staleAttachmentsChannel = Channel<List<Attachment>>()
    private val toolBannerAttachmentsChannel = Channel<List<Attachment>>()
    private val pinnedTranslationsChannel = Channel<List<Translation>>()

    @Before
    fun setup() {
        attachmentsApi = mock()
        dao = mock {
            on { transaction(any(), any<() -> Any>()) } doAnswer { it.getArgument<() -> Any>(1).invoke() }
            on { getAsFlow(QUERY_STALE_ATTACHMENTS) } doReturn staleAttachmentsChannel.consumeAsFlow()
            on { getAsFlow(QUERY_TOOL_BANNER_ATTACHMENTS) } doReturn toolBannerAttachmentsChannel.consumeAsFlow()
            on { getAsFlow(QUERY_PINNED_TRANSLATIONS) } doReturn pinnedTranslationsChannel.consumeAsFlow()
        }
        eventBus = mock()
        fs = mock {
            onBlocking { rootDir() } doReturn resourcesDir
            onBlocking { exists() } doReturn true
        }
        observer = mock()
        settings = mock()
        translationsApi = mock()
        workManager = mock {
            on { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } doReturn mock()
        }
        testScope = TestCoroutineScope()

        downloadManager = GodToolsDownloadManager(
            attachmentsApi,
            dao,
            eventBus,
            fs,
            settings,
            translationsApi,
            { workManager },
            testScope,
            testScope.coroutineContext
        )
    }

    @After
    fun cleanup() {
        staleAttachmentsChannel.close()
        toolBannerAttachmentsChannel.close()
        runBlocking { downloadManager.shutdown() }
        testScope.cleanupTestCoroutines()
    }

    // region pinTool()/unpinTool()
    @Test
    fun verifyPinTool() {
        runBlocking { downloadManager.pinTool(TOOL) }
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }

    @Test
    fun verifyPinToolAsync() {
        runBlocking { downloadManager.pinToolAsync(TOOL).join() }

        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }

    @Test
    fun verifyUnpinTool() {
        runBlocking { downloadManager.unpinTool(TOOL) }
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertFalse(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }

    @Test
    fun verifyUnpinToolAsync() {
        runBlocking { downloadManager.unpinToolAsync(TOOL).join() }

        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertFalse(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }
    // endregion pinTool()/unpinTool()

    // region pinLanguage()/unpinLanguage()
    @Test
    fun verifyPinLanguage() {
        runBlocking { downloadManager.pinLanguage(Locale.FRENCH) }
        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
    }

    @Test
    fun verifyPinLanguageAsync() {
        runBlocking { downloadManager.pinLanguageAsync(Locale.FRENCH).join() }

        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
    }

    @Test
    fun verifyUnpinLanguage() {
        runBlocking { downloadManager.unpinLanguage(Locale.FRENCH) }
        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertFalse(firstValue.isAdded)
        }
    }
    // endregion pinLanguage()/unpinLanguage()

    // region Download Progress
    @Test
    fun verifyDownloadProgressLiveDataReused() {
        assertSame(
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.ENGLISH),
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.ENGLISH)
        )
        assertNotSame(
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.ENGLISH),
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH)
        )
    }

    @Test
    fun verifyDownloadProgressLiveData() {
        val translationKey = TranslationKey(TOOL, Locale.ENGLISH)
        val liveData = downloadManager.getDownloadProgressLiveData(TOOL, Locale.ENGLISH)

        liveData.observeForever(observer)
        verify(observer, never()).onChanged(any())

        // start download
        downloadManager.startProgress(translationKey)
        argumentCaptor<DownloadProgress> {
            verify(observer).onChanged(capture())

            assertEquals(DownloadProgress.INITIAL, firstValue)
        }

        // update progress
        reset(observer)
        downloadManager.updateProgress(translationKey, 5, 0)
        downloadManager.updateProgress(translationKey, 5, 10)
        argumentCaptor<DownloadProgress> {
            verify(observer, times(2)).onChanged(capture())

            assertEquals(DownloadProgress(5, 0), firstValue)
            assertEquals(DownloadProgress(5, 10), lastValue)
        }

        // finish download
        reset(observer)
        downloadManager.finishDownload(translationKey)
        nullableArgumentCaptor<DownloadProgress> {
            verify(observer).onChanged(capture())

            assertNull(firstValue)
        }
    }
    // endregion Download Progress

    // region Attachments
    private val attachment = Attachment().apply {
        id = 1
        filename = "image.jpg"
        sha256 = "sha256"
    }
    private val file = getTmpFile()
    private val testData = Random.nextBytes(16 * 1024)

    @Test
    fun verifyDownloadStaleAttachments() {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        stubbing(attachmentsApi) { onBlocking { download(any()) } doReturn Response.success(response) }
        fs.stub { onBlocking { attachment.getFile(this) } doReturn file }

        assertTrue(staleAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        assertTrue(staleAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        verify(dao, never()).find<Attachment>(attachment.id)

        assertTrue(staleAttachmentsChannel.trySendBlocking(listOf(attachment)).isSuccess)
        // this will block until the previous list has been processed
        assertTrue(staleAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyDownloadToolBannerAttachments() {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        stubbing(attachmentsApi) { onBlocking { download(any()) } doReturn Response.success(response) }
        fs.stub { onBlocking { attachment.getFile(this) } doReturn file }

        assertTrue(toolBannerAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        assertTrue(toolBannerAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        verify(dao, never()).find<Attachment>(attachment.id)

        assertTrue(toolBannerAttachmentsChannel.trySendBlocking(listOf(attachment)).isSuccess)
        // this will block until the previous list has been processed
        assertTrue(toolBannerAttachmentsChannel.trySendBlocking(emptyList()).isSuccess)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    // region downloadAttachment()
    @Test
    fun `downloadAttachment()`() = runTest {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        whenever(attachmentsApi.download(any())) doReturn Response.success(response)
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Already Downloaded`() = runTest {
        attachment.isDownloaded = true
        stubbing(dao) {
            on { find<Attachment>(attachment.id) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }

        downloadManager.downloadAttachment(attachment.id)
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).find<LocalFile>(attachment.localFilename!!)
        verify(attachmentsApi, never()).download(any())
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing`() = runTest {
        attachment.isDownloaded = true
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        whenever(attachmentsApi.download(any())) doReturn Response.success(response)
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing, fails download`() = runTest {
        attachment.isDownloaded = true
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        whenever(attachmentsApi.download(any())) doAnswer { throw IOException() }
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertFalse(file.exists())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao, never()).updateOrInsert(any())
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertFalse(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Download fails`() = runTest {
        whenever(dao.find<Attachment>(attachment.id)) doReturn attachment
        whenever(attachmentsApi.download(any())) doAnswer { throw IOException() }

        downloadManager.downloadAttachment(attachment.id)
        verify(dao).find<Attachment>(attachment.id)
        verify(attachmentsApi).download(any())
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(AttachmentUpdateEvent)
        assertFalse(attachment.isDownloaded)
    }
    // endregion downloadAttachment()

    @Test
    fun verifyImportAttachment() {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        fs.stub { onBlocking { attachment.getFile(this) } doReturn file }

        runBlocking { testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) } }
        assertArrayEquals(testData, file.readBytes())
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        fs.stub {
            onBlocking { exists() } doReturn false
            onBlocking { attachment.getFile(this) } doReturn file
        }

        runBlocking { testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) } }
        assertFalse(file.exists())
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(any())
    }

    @Test
    fun verifyImportAttachmentAttachmentAlreadyDownloaded() {
        attachment.isDownloaded = true
        dao.stub {
            on { find<Attachment>(attachment.id) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }
        fs.stub { onBlocking { attachment.getFile(this) } doReturn file }

        runBlocking { testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) } }
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(any())
        verifyBlocking(fs, never()) { file(any()) }
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() {
        attachment.isDownloaded = false
        dao.stub {
            on { find<Attachment>(attachment.id) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }
        fs.stub { onBlocking { attachment.getFile(this) } doReturn file }

        runBlocking { testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) } }
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        verifyBlocking(fs, never()) { file(any()) }
        verify(dao, never()).updateOrInsert(any())
        assertTrue(attachment.isDownloaded)
    }

    private fun Attachment.asLocalFile() = LocalFile(localFilename!!)
    // endregion Attachments

    // region Translations
    private val translation = Translation().apply {
        id = Random.nextLong()
        toolCode = TOOL
        languageCode = Locale.FRENCH
        isDownloaded = false
    }

    // region downloadLatestPublishedTranslation()
    @Test
    fun `downloadLatestPublishedTranslation()`() = runTest {
        val files = Array(3) { getTmpFile() }
        downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
        whenever(dao.getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)) doReturn translation
        fs.stub {
            onBlocking { file("a.txt") } doReturn files[0]
            onBlocking { file("b.txt") } doReturn files[1]
            onBlocking { file("c.txt") } doReturn files[2]
        }
        val response: ResponseBody = mock { on { byteStream() } doReturn getInputStreamForResource("abc.zip") }
        whenever(translationsApi.download(translation.id)) doReturn Response.success(response)

        assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        verify(dao).getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
        verify(translationsApi).download(translation.id)
        assertArrayEquals("a".repeat(1024).toByteArray(), files[0].readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files[1].readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files[2].readBytes())
        verify(dao).updateOrInsert(eq(LocalFile("a.txt")))
        verify(dao).updateOrInsert(eq(LocalFile("b.txt")))
        verify(dao).updateOrInsert(eq(LocalFile("c.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "a.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "b.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "c.txt")))
        verify(dao).update(translation, TranslationTable.COLUMN_DOWNLOADED)
        assertTrue(translation.isDownloaded)
        verify(eventBus).post(TranslationUpdateEvent)
        verifyNoInteractions(workManager)
        argumentCaptor<DownloadProgress> {
            verify(observer, atLeastOnce()).onChanged(capture())
            assertNull(lastValue)
        }
    }

    @Test
    fun `downloadLatestPublishedTranslation() - API IOException`() = runTest {
        downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
        whenever(dao.getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)) doReturn translation
        whenever(translationsApi.download(translation.id)) doAnswer { throw IOException() }
        clearInvocations(dao)

        assertFalse(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        verify(dao).getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
        verify(translationsApi).download(translation.id)
        verify(workManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        verifyNoMoreInteractions(dao)
        verifyNoInteractions(eventBus)
        argumentCaptor<DownloadProgress> {
            verify(observer, atLeastOnce()).onChanged(capture())
            assertNull(lastValue)
        }
    }
    // endregion downloadLatestPublishedTranslation()

    @Test
    fun verifyImportTranslation() {
        val files = Array(3) { getTmpFile() }
        downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
        fs.stub {
            onBlocking { file("a.txt") } doReturn files[0]
            onBlocking { file("b.txt") } doReturn files[1]
            onBlocking { file("c.txt") } doReturn files[2]
        }

        runBlocking { downloadManager.importTranslation(translation, getInputStreamForResource("abc.zip"), -1) }
        assertArrayEquals("a".repeat(1024).toByteArray(), files[0].readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files[1].readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files[2].readBytes())
        verify(dao).updateOrInsert(eq(LocalFile("a.txt")))
        verify(dao).updateOrInsert(eq(LocalFile("b.txt")))
        verify(dao).updateOrInsert(eq(LocalFile("c.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "a.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "b.txt")))
        verify(dao).updateOrInsert(eq(TranslationFile(translation, "c.txt")))
        verify(dao).update(translation, TranslationTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(TranslationUpdateEvent)
        argumentCaptor<DownloadProgress> {
            verify(observer, atLeastOnce()).onChanged(capture())
            assertNull(lastValue)
        }
    }

    @Test
    fun verifyPruneStaleTranslations() {
        val valid1 = Translation().apply {
            toolCode = TOOL
            languageCode = Locale.ENGLISH
            isDownloaded = true
        }
        val valid2 = Translation().apply {
            toolCode = TOOL
            languageCode = Locale.FRENCH
            isDownloaded = true
        }
        val valid3 = Translation().apply {
            toolCode = "$TOOL$TOOL"
            languageCode = Locale.ENGLISH
            isDownloaded = true
        }
        val invalid = Translation().apply {
            toolCode = TOOL
            languageCode = Locale.ENGLISH
            isDownloaded = true
        }
        whenever(dao.get(argThat<Query<*>> { table.type == Translation::class.java }))
            .thenReturn(listOf(valid1, valid2, invalid, valid3))

        runBlocking { downloadManager.pruneStaleTranslations() }
        verify(dao).update(invalid, TranslationTable.COLUMN_DOWNLOADED)
        assertFalse(invalid.isDownloaded)
        verify(dao, never()).update(valid1, TranslationTable.COLUMN_DOWNLOADED)
        verify(dao, never()).update(valid2, TranslationTable.COLUMN_DOWNLOADED)
        verify(dao, never()).update(valid3, TranslationTable.COLUMN_DOWNLOADED)
        assertTrue(valid1.isDownloaded)
        assertTrue(valid2.isDownloaded)
        assertTrue(valid3.isDownloaded)
        verify(eventBus).post(TranslationUpdateEvent)
    }
    // endregion Translations

    // region Cleanup
    @Test
    fun verifyCleanupActorAutomaticRuns() {
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY_INITIAL - 1)
        testScope.runCurrent()
        assertCleanupActorRan(0)
        testScope.testScheduler.advanceTimeBy(1)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY - 1)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(1)
        testScope.runCurrent()
        assertCleanupActorRan(2)
    }

    @Test
    fun verifyCleanupActorBeforeInitialRun() {
        assertCleanupActorRan(0)
        runBlocking { downloadManager.cleanupActor.send(GodToolsDownloadManager.RunCleanup) }
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY_INITIAL)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY - CLEANUP_DELAY_INITIAL - 1)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(1)
        testScope.runCurrent()
        assertCleanupActorRan(2)
    }

    @Test
    fun verifyCleanupActorRunsWhenTriggered() {
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY_INITIAL)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.testScheduler.advanceTimeBy(2000)
        testScope.runCurrent()
        runBlocking { downloadManager.cleanupActor.send(GodToolsDownloadManager.RunCleanup) }
        assertCleanupActorRan(2)
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY - 1)
        testScope.runCurrent()
        assertCleanupActorRan(2)
        testScope.testScheduler.advanceTimeBy(1)
        testScope.runCurrent()
        assertCleanupActorRan(3)
    }

    private fun assertCleanupActorRan(times: Int = 1) {
        inOrder(dao, fs) {
            repeat(times) {
                runBlocking { verify(fs).rootDir() }
                verify(dao).get(argThat<Query<*>> { table.type == LocalFile::class.java })
                verify(dao).get(argThat<Query<*>> { table.type == TranslationFile::class.java })
                verify(dao).get(argThat<Query<*>> { table.type == LocalFile::class.java })
                runBlocking { verify(fs).rootDir() }
            }
            verify(dao, never()).get(any<Query<*>>())
            runBlocking { verify(fs, never()).rootDir() }
        }
    }

    @Test
    fun verifyDetectMissingFiles() {
        val file = getTmpFile(true)
        val missingFile = getTmpFile()
        whenever(dao.get(any<Query<LocalFile>>())).thenReturn(listOf(LocalFile(file.name), LocalFile(missingFile.name)))
        fs.stub {
            onBlocking { rootDir() } doReturn file.parentFile!!
            onBlocking { file(any()) } doAnswer { File(file.parentFile, it.getArgument(0)) }
        }

        runBlocking { downloadManager.detectMissingFiles() }
        verify(dao, never()).delete(LocalFile(file.name))
        verify(dao).delete(LocalFile(missingFile.name))
    }

    @Test
    fun verifyCleanupFilesystem() {
        val orphan = spy(getTmpFile(true))
        val translation = TranslationFile(1, orphan.name)
        val localFile = LocalFile(orphan.name)
        val keep = spy(getTmpFile(true))
        dao.stub {
            on { get(argThat<Query<*>> { table.type == TranslationFile::class.java }) } doReturn listOf(translation)
            on { get(argThat<Query<*>> { table.type == LocalFile::class.java }) } doReturn listOf(localFile)
            val keepLocalFile = LocalFile(keep.name)
            on { find<LocalFile>(keep.name) } doReturn keepLocalFile
        }
        fs.stub {
            onBlocking { file(keep.name) } doReturn keep
            onBlocking { file(orphan.name) } doReturn orphan
        }

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItem(orphan))
        runBlocking { downloadManager.cleanFilesystem() }
        verify(dao).delete(translation)
        verify(dao).delete(localFile)
        verify(orphan).delete()
        verify(keep, never()).delete()
        assertEquals(setOf(keep), resourcesDir.listFiles()!!.toSet())
    }
    // endregion Cleanup

    private fun getTmpFile(create: Boolean = false) =
        File.createTempFile("test-", null, resourcesDir).also { if (!create) it.delete() }
    private fun getInputStreamForResource(name: String) = this::class.java.getResourceAsStream(name)!!
}
