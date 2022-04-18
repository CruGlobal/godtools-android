package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
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

    private val attachmentsApi = mockk<AttachmentsApi>()
    private lateinit var dao: GodToolsDao
    private val eventBus = mockk<EventBus>(relaxUnitFun = true)
    private lateinit var fs: ToolFileSystem
    private val settings = mockk<Settings> {
        every { isLanguageProtected(any()) } returns false
        every { parallelLanguage } returns null
    }
    private val translationsApi = mockk<TranslationsApi>()
    private val workManager = mockk<WorkManager> {
        every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
    }
    private lateinit var testScope: TestCoroutineScope

    private lateinit var downloadManager: GodToolsDownloadManager

    private val downloadProgress = mutableListOf<DownloadProgress?>()
    private val observer = mockk<Observer<DownloadProgress?>> {
        every { onChanged(captureNullable(downloadProgress)) } returns Unit
    }

    @Before
    fun setup() {
        dao = mock {
            on { transaction(any(), any<() -> Any>()) } doAnswer { it.getArgument<() -> Any>(1).invoke() }
        }
        fs = mock {
            onBlocking { rootDir() } doReturn resourcesDir
            onBlocking { exists() } doReturn true
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
        runBlocking { downloadManager.shutdown() }
        testScope.cleanupTestCoroutines()
    }

    // region pinTool()/unpinTool()
    @Test
    fun verifyPinTool() = runTest {
        downloadManager.pinTool(TOOL)
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(exactly = 1) { eventBus.post(ToolUpdateEvent) }
        confirmVerified(eventBus)
    }

    @Test
    fun verifyPinToolAsync() = runTest {
        downloadManager.pinToolAsync(TOOL).join()
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(exactly = 1) { eventBus.post(ToolUpdateEvent) }
        confirmVerified(eventBus)
    }

    @Test
    fun verifyUnpinTool() = runTest {
        downloadManager.unpinTool(TOOL)
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertFalse(firstValue.isAdded)
        }
        verify(exactly = 1) { eventBus.post(ToolUpdateEvent) }
        confirmVerified(eventBus)
    }

    @Test
    fun verifyUnpinToolAsync() = runTest {
        downloadManager.unpinToolAsync(TOOL).join()
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertFalse(firstValue.isAdded)
        }
        verify(exactly = 1) { eventBus.post(ToolUpdateEvent) }
        confirmVerified(eventBus)
    }
    // endregion pinTool()/unpinTool()

    // region pinLanguage()/unpinLanguage()
    @Test
    fun verifyPinLanguage() = runTest {
        downloadManager.pinLanguage(Locale.FRENCH)
        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
    }

    @Test
    fun verifyPinLanguageAsync() = runTest {
        downloadManager.pinLanguageAsync(Locale.FRENCH).join()

        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
    }

    @Test
    fun verifyUnpinLanguage() = runTest {
        downloadManager.unpinLanguage(Locale.FRENCH)
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
        verifyAll { observer wasNot Called }

        // start download
        downloadManager.startProgress(translationKey)
        verify(exactly = 1) { observer.onChanged(any()) }
        assertEquals(DownloadProgress.INITIAL, downloadProgress[0])

        // update progress
        downloadManager.updateProgress(translationKey, 5, 0)
        downloadManager.updateProgress(translationKey, 5, 10)
        verify(exactly = 3) { observer.onChanged(any()) }
        assertEquals(DownloadProgress(5, 0), downloadProgress[1])
        assertEquals(DownloadProgress(5, 10), downloadProgress[2])

        // finish download
        downloadManager.finishDownload(translationKey)
        verify(exactly = 4) { observer.onChanged(any()) }
        assertNull(downloadProgress[3])
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

    // region downloadAttachment()
    @Test
    fun `downloadAttachment()`() = runTest {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        coEvery { attachmentsApi.download(any()) } returns Response.success(response)
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(exactly = 1) { eventBus.post(AttachmentUpdateEvent) }
        confirmVerified(eventBus)
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
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify {
            attachmentsApi wasNot Called
            eventBus wasNot Called
        }
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing`() = runTest {
        attachment.isDownloaded = true
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        coEvery { attachmentsApi.download(any()) } returns Response.success(response)
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(exactly = 1) { eventBus.post(AttachmentUpdateEvent) }
        confirmVerified(eventBus)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing, fails download`() = runTest {
        attachment.isDownloaded = true
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        coEvery { attachmentsApi.download(any()) } throws IOException()
        whenever(attachment.getFile(fs)) doReturn file

        downloadManager.downloadAttachment(attachment.id)
        assertFalse(file.exists())
        verify(dao).find<Attachment>(attachment.id)
        verify(dao, never()).updateOrInsert(any())
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(exactly = 1) { eventBus.post(AttachmentUpdateEvent) }
        confirmVerified(eventBus)
        assertFalse(attachment.isDownloaded)
    }

    @Test
    fun `downloadAttachment() - Download fails`() = runTest {
        whenever(dao.find<Attachment>(attachment.id)) doReturn attachment
        coEvery { attachmentsApi.download(any()) } throws IOException()

        downloadManager.downloadAttachment(attachment.id)
        verify(dao).find<Attachment>(attachment.id)
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        coVerify(exactly = 1) {
            attachmentsApi.download(any())
            eventBus wasNot Called
        }
        confirmVerified(attachmentsApi)
        assertFalse(attachment.isDownloaded)
    }
    // endregion downloadAttachment()

    @Test
    fun verifyImportAttachment() = runTest {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        whenever(attachment.getFile(fs)) doReturn file

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertArrayEquals(testData, file.readBytes())
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(exactly = 1) { eventBus.post(AttachmentUpdateEvent) }
        confirmVerified(eventBus)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() = runTest {
        whenever(dao.find<Attachment>(attachment.id)).thenReturn(attachment)
        fs.stub {
            onBlocking { exists() } doReturn false
            onBlocking { attachment.getFile(this) } doReturn file
        }

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertFalse(file.exists())
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify { eventBus wasNot Called }
    }

    @Test
    fun verifyImportAttachmentAttachmentAlreadyDownloaded() = runTest {
        attachment.isDownloaded = true
        dao.stub {
            on { find<Attachment>(attachment.id) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }
        whenever(attachment.getFile(fs)) doReturn file

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify { eventBus wasNot Called }
        verifyBlocking(fs, never()) { file(any()) }
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() = runTest {
        attachment.isDownloaded = false
        dao.stub {
            on { find<Attachment>(attachment.id) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }
        whenever(attachment.getFile(fs)) doReturn file

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(exactly = 1) { eventBus.post(AttachmentUpdateEvent) }
        confirmVerified(eventBus)
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
        coEvery { translationsApi.download(translation.id) } returns Response.success(response)

        assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        verify(dao).getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
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
        coVerify(exactly = 1) {
            translationsApi.download(translation.id)
            workManager wasNot Called
            eventBus.post(TranslationUpdateEvent)
        }
        confirmVerified(translationsApi, eventBus)
        verify { observer.onChanged(any()) }
        assertNull(downloadProgress.last())
    }

    @Test
    fun `downloadLatestPublishedTranslation() - API IOException`() = runTest {
        downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
        whenever(dao.getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)) doReturn translation
        coEvery { translationsApi.download(translation.id) } throws IOException()
        clearInvocations(dao)

        assertFalse(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        verify(dao).getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
        coVerify(exactly = 1) {
            translationsApi.download(translation.id)
            workManager.enqueueUniqueWork(any(), ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
            eventBus wasNot Called
        }
        confirmVerified(translationsApi, workManager)
        verifyNoMoreInteractions(dao)
        verify { observer.onChanged(any()) }
        assertNull(downloadProgress.last())
    }
    // endregion downloadLatestPublishedTranslation()

    @Test
    fun verifyImportTranslation() = runTest {
        val files = Array(3) { getTmpFile() }
        downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
        fs.stub {
            onBlocking { file("a.txt") } doReturn files[0]
            onBlocking { file("b.txt") } doReturn files[1]
            onBlocking { file("c.txt") } doReturn files[2]
        }

        downloadManager.importTranslation(translation, getInputStreamForResource("abc.zip"), -1)
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
        verify(exactly = 1) { eventBus.post(TranslationUpdateEvent) }
        confirmVerified(eventBus)
        verify { observer.onChanged(any()) }
        assertNull(downloadProgress.last())
    }

    @Test
    fun verifyPruneStaleTranslations() = runTest {
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

        downloadManager.pruneStaleTranslations()
        verify(dao).update(invalid, TranslationTable.COLUMN_DOWNLOADED)
        assertFalse(invalid.isDownloaded)
        verify(dao, never()).update(valid1, TranslationTable.COLUMN_DOWNLOADED)
        verify(dao, never()).update(valid2, TranslationTable.COLUMN_DOWNLOADED)
        verify(dao, never()).update(valid3, TranslationTable.COLUMN_DOWNLOADED)
        assertTrue(valid1.isDownloaded)
        assertTrue(valid2.isDownloaded)
        assertTrue(valid3.isDownloaded)
        verify(exactly = 1) { eventBus.post(TranslationUpdateEvent) }
        confirmVerified(eventBus)
    }
    // endregion Translations

    // region Cleanup
    @Test
    fun verifyCleanupActorInitialRun() = runTest {
        testScope.testScheduler.advanceTimeBy(CLEANUP_DELAY)
        assertCleanupActorRan(0)
        testScope.runCurrent()
        assertCleanupActorRan(1)
        testScope.advanceUntilIdle()
        assertCleanupActorRan(1)
    }

    @Test
    fun verifyCleanupActorRunsWhenTriggered() = runTest {
        testScope.advanceUntilIdle()
        assertCleanupActorRan(1)
        downloadManager.cleanupActor.send(Unit)
        testScope.advanceUntilIdle()
        assertCleanupActorRan(2)
    }

    private suspend fun assertCleanupActorRan(times: Int = 1) {
        inOrder(dao, fs) {
            repeat(times) {
                verify(fs).exists()
                verify(fs).rootDir()
                verify(dao).get(argThat<Query<*>> { table.type == LocalFile::class.java })

                verify(fs).exists()
                verify(dao).get(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES)
                verify(dao).get(QUERY_CLEAN_ORPHANED_LOCAL_FILES)
                verify(fs).rootDir()
            }
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun verifyDetectMissingFiles() = runTest {
        val file = getTmpFile(true)
        val missingFile = getTmpFile()
        whenever(dao.get(any<Query<LocalFile>>())).thenReturn(listOf(LocalFile(file.name), LocalFile(missingFile.name)))
        fs.stub {
            onBlocking { rootDir() } doReturn file.parentFile!!
            onBlocking { file(any()) } doAnswer { File(file.parentFile, it.getArgument(0)) }
        }

        downloadManager.detectMissingFiles()
        verify(dao, never()).delete(LocalFile(file.name))
        verify(dao).delete(LocalFile(missingFile.name))
    }

    @Test
    fun verifyCleanupFilesystem() = runTest {
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
        downloadManager.cleanFilesystem()
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
