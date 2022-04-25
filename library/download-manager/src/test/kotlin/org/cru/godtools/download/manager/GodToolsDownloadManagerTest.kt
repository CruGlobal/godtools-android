package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import io.mockk.verifySequence
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.internal.http.RealResponseBody
import okio.buffer
import okio.source
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
    private val dao = mockk<GodToolsDao>(relaxUnitFun = true) {
        every { transaction(any(), any<() -> Any>()) } answers { (it.invocation.args[1] as () -> Any).invoke() }
        excludeRecords { transaction(any(), any()) }
    }
    private val eventBus = mockk<EventBus>(relaxUnitFun = true)
    private val files = mutableMapOf<String, File>()
    private val fs = mockk<ToolFileSystem> {
        coEvery { rootDir() } returns resourcesDir
        coEvery { exists() } returns true
        coEvery { file(any()) } answers { files.getOrPut(it.invocation.args[0] as String) { getTmpFile() } }
    }
    private val settings = mockk<Settings> {
        every { isLanguageProtected(any()) } returns false
        every { parallelLanguage } returns null
    }
    private val translationsApi = mockk<TranslationsApi>()
    private val workManager = mockk<WorkManager> {
        every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
    }

    private val tool = slot<Tool>()
    private val language = slot<Language>()

    private val downloadProgress = mutableListOf<DownloadProgress?>()
    private val observer = mockk<Observer<DownloadProgress?>> {
        every { onChanged(captureNullable(downloadProgress)) } returns Unit
    }

    private inline fun TestScope.withDownloadManager(
        dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
        enableCleanupActor: Boolean = false,
        block: (GodToolsDownloadManager) -> Unit
    ) {
        val downloadManager = GodToolsDownloadManager(
            attachmentsApi,
            dao,
            eventBus,
            fs,
            mockk(),
            settings,
            translationsApi,
            { workManager },
            this,
            dispatcher
        )
        try {
            if (!enableCleanupActor) downloadManager.cleanupActor.close()
            block(downloadManager)
        } finally {
            downloadManager.cleanupActor.close()
        }
    }

    // region pinTool()/unpinTool()
    @Test
    fun verifyPinTool() = runTest {
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.pinTool(TOOL) }
        assertEquals(TOOL, tool.captured.code)
        assertTrue(tool.captured.isAdded)
        verifyAll {
            dao.update(tool.captured, ToolTable.COLUMN_ADDED)
            eventBus.post(ToolUpdateEvent)
        }
    }

    @Test
    fun verifyPinToolAsync() = runTest {
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.pinToolAsync(TOOL).join() }
        assertEquals(TOOL, tool.captured.code)
        assertTrue(tool.captured.isAdded)
        verifyAll {
            dao.update(tool.captured, ToolTable.COLUMN_ADDED)
            eventBus.post(ToolUpdateEvent)
        }
    }

    @Test
    fun verifyUnpinTool() = runTest {
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.unpinTool(TOOL) }
        assertEquals(TOOL, tool.captured.code)
        assertFalse(tool.captured.isAdded)
        verifyAll {
            dao.update(tool.captured, ToolTable.COLUMN_ADDED)
            eventBus.post(ToolUpdateEvent)
        }
    }

    @Test
    fun verifyUnpinToolAsync() = runTest {
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.unpinToolAsync(TOOL).join() }
        assertEquals(TOOL, tool.captured.code)
        assertFalse(tool.captured.isAdded)
        verifyAll {
            dao.update(tool.captured, ToolTable.COLUMN_ADDED)
            eventBus.post(ToolUpdateEvent)
        }
    }
    // endregion pinTool()/unpinTool()

    // region pinLanguage()/unpinLanguage()
    @Test
    fun verifyPinLanguage() = runTest {
        every { dao.update(capture(language), LanguageTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.pinLanguage(Locale.FRENCH) }
        assertEquals(Locale.FRENCH, language.captured.code)
        assertTrue(language.captured.isAdded)
        verifyAll { dao.update(language.captured, LanguageTable.COLUMN_ADDED) }
    }

    @Test
    fun verifyPinLanguageAsync() = runTest {
        every { dao.update(capture(language), LanguageTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.pinLanguageAsync(Locale.FRENCH).join() }
        assertEquals(Locale.FRENCH, language.captured.code)
        assertTrue(language.captured.isAdded)
        verifyAll { dao.update(language.captured, LanguageTable.COLUMN_ADDED) }
    }

    @Test
    fun verifyUnpinLanguage() = runTest {
        every { dao.update(capture(language), LanguageTable.COLUMN_ADDED) } returns 1

        withDownloadManager { it.unpinLanguage(Locale.FRENCH) }
        assertEquals(Locale.FRENCH, language.captured.code)
        assertFalse(language.captured.isAdded)
        verifyAll { dao.update(language.captured, LanguageTable.COLUMN_ADDED) }
    }
    // endregion pinLanguage()/unpinLanguage()

    // region Download Progress
    @Test
    fun verifyDownloadProgressLiveDataReused() = runTest {
        withDownloadManager {
            assertSame(
                it.getDownloadProgressLiveData(TOOL, Locale.ENGLISH),
                it.getDownloadProgressLiveData(TOOL, Locale.ENGLISH)
            )
            assertNotSame(
                it.getDownloadProgressLiveData(TOOL, Locale.ENGLISH),
                it.getDownloadProgressLiveData(TOOL, Locale.FRENCH)
            )
        }
    }

    @Test
    fun verifyDownloadProgressLiveData() = runTest {
        val translationKey = TranslationKey(TOOL, Locale.ENGLISH)
        withDownloadManager { downloadManager ->
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

    @Before
    fun setupAttachmentMocks() {
        every { dao.find<Attachment>(attachment.id) } returns attachment
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns attachment.asLocalFile()
        every { dao.update(any<Attachment>(), AttachmentTable.COLUMN_DOWNLOADED) } returns 1

        excludeRecords {
            dao.find<Attachment>(attachment.id)
            dao.find<LocalFile>(attachment.localFilename!!)
        }
    }

    // region downloadAttachment()
    @Test
    fun `downloadAttachment()`() = runTest {
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        val response = RealResponseBody(null, 0, testData.inputStream().source().buffer())
        coEvery { attachmentsApi.download(any()) } returns Response.success(response)
        coEvery { attachment.getFile(fs) } returns file

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            dao.updateOrInsert(attachment.asLocalFile())
            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
            eventBus.post(AttachmentUpdateEvent)
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded`() = runTest {
        attachment.isDownloaded = true

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertTrue(attachment.isDownloaded)
        verify {
            attachmentsApi wasNot Called
            eventBus wasNot Called
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing`() = runTest {
        attachment.isDownloaded = true
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        val response = RealResponseBody(null, 0, testData.inputStream().source().buffer())
        coEvery { attachmentsApi.download(attachment.id) } returns Response.success(response)
        coEvery { attachment.getFile(fs) } returns file

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            dao.updateOrInsert(attachment.asLocalFile())
            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
            eventBus.post(AttachmentUpdateEvent)
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing, fails download`() = runTest {
        attachment.isDownloaded = true
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        coEvery { attachmentsApi.download(attachment.id) } throws IOException()
        coEvery { attachment.getFile(fs) } returns file

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertFalse(file.exists())
        assertFalse(attachment.isDownloaded)
        verify(inverse = true) { dao.updateOrInsert(attachment.asLocalFile()) }
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
            eventBus.post(AttachmentUpdateEvent)
        }
    }

    @Test
    fun `downloadAttachment() - Download fails`() = runTest {
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        coEvery { attachmentsApi.download(attachment.id) } throws IOException()

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertFalse(attachment.isDownloaded)
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            eventBus wasNot Called
        }
    }
    // endregion downloadAttachment()

    @Test
    fun verifyImportAttachment() = runTest {
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        coEvery { attachment.getFile(fs) } returns file

        withDownloadManager { downloadManager ->
            testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        }
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        verifySequence {
            dao.updateOrInsert(attachment.asLocalFile())
            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
            eventBus.post(AttachmentUpdateEvent)
        }
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() = runTest {
        coEvery { fs.exists() } returns false
        coEvery { attachment.getFile(fs) } returns file

        withDownloadManager { downloadManager ->
            testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        }
        assertFalse(file.exists())
        verify {
            dao wasNot Called
            eventBus wasNot Called
        }
    }

    @Test
    fun verifyImportAttachmentAttachmentAlreadyDownloaded() = runTest {
        attachment.isDownloaded = true

        withDownloadManager { downloadManager ->
            testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        }
        assertTrue(attachment.isDownloaded)
        coVerify(inverse = true) {
            fs.file(any())
            dao.updateOrInsert(any())
            dao.update(any())
        }
        verify { eventBus wasNot Called }
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() = runTest {
        attachment.isDownloaded = false

        withDownloadManager { downloadManager ->
            testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        }
        assertTrue(attachment.isDownloaded)
        verifySequence {
            dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
            eventBus.post(AttachmentUpdateEvent)
        }
        coVerify(inverse = true) {
            fs.file(any())
            dao.updateOrInsert(any())
        }
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
        every { dao.getLatestTranslation(translation.toolCode, translation.languageCode, any()) } returns translation
        every { dao.find<LocalFile>(any<String>()) } returns null
        val response = RealResponseBody(null, 0, getInputStreamForResource("abc.zip").source().buffer())
        coEvery { translationsApi.download(translation.id) } returns Response.success(response)

        // HACK: suppress dao calls from pruneTranslations()
        every { dao.get(QUERY_STALE_TRANSLATIONS) } returns emptyList()
        excludeRecords { dao.get(QUERY_STALE_TRANSLATIONS) }

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        }
        assertTrue(translation.isDownloaded)
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files["c.txt"]!!.readBytes())

        coVerifyAll {
            dao.getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
            translationsApi.download(translation.id)
            dao.find<LocalFile>("a.txt")
            dao.find<LocalFile>("b.txt")
            dao.find<LocalFile>("c.txt")
            dao.updateOrInsert(LocalFile("a.txt"))
            dao.updateOrInsert(LocalFile("b.txt"))
            dao.updateOrInsert(LocalFile("c.txt"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "c.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
            eventBus.post(TranslationUpdateEvent)
            workManager wasNot Called
            observer.onChanged(any())
        }
        assertNull(downloadProgress.last())
    }

    @Test
    fun `downloadLatestPublishedTranslation() - API IOException`() = runTest {
        every { dao.getLatestTranslation(translation.toolCode, translation.languageCode, any()) } returns translation
        coEvery { translationsApi.download(translation.id) } throws IOException()

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            assertFalse(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        }
        assertNull(downloadProgress.last())
        coVerifyAll {
            dao.getLatestTranslation(TOOL, Locale.FRENCH, isPublished = true)
            translationsApi.download(translation.id)
            workManager.enqueueUniqueWork(any(), ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
            observer.onChanged(any())
        }
    }
    // endregion downloadLatestPublishedTranslation()

    @Test
    fun verifyImportTranslation() = runTest {
        every { dao.getLatestTranslation(any(), any(), any(), any()) } returns null
        every { dao.find<LocalFile>(any<String>()) } returns null

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            downloadManager.importTranslation(translation, getInputStreamForResource("abc.zip"), -1)
        }
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files["c.txt"]!!.readBytes())
        verifyAll {
            dao.getLatestTranslation(translation.toolCode, translation.languageCode, true, true)
            dao.find<LocalFile>("a.txt")
            dao.updateOrInsert(LocalFile("a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            dao.find<LocalFile>("b.txt")
            dao.find<LocalFile>("c.txt")
            dao.updateOrInsert(LocalFile("b.txt"))
            dao.updateOrInsert(LocalFile("c.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "c.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
            eventBus.post(TranslationUpdateEvent)
        }
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
        every { dao.get(QUERY_STALE_TRANSLATIONS) } returns listOf(valid1, valid2, invalid, valid3)
        setupCleanupActorMocks()

        withDownloadManager(enableCleanupActor = true) { it.pruneStaleTranslations() }
        assertFalse(invalid.isDownloaded)
        assertTrue(valid1.isDownloaded)
        assertTrue(valid2.isDownloaded)
        assertTrue(valid3.isDownloaded)
        verify {
            dao.update(invalid, TranslationTable.COLUMN_DOWNLOADED)
            eventBus.post(TranslationUpdateEvent)
        }
        verify(inverse = true) {
            dao.update(valid1, TranslationTable.COLUMN_DOWNLOADED)
            dao.update(valid2, TranslationTable.COLUMN_DOWNLOADED)
            dao.update(valid3, TranslationTable.COLUMN_DOWNLOADED)
        }
        confirmVerified(eventBus)
    }
    // endregion Translations

    // region Cleanup
    @Test
    fun verifyCleanupActorInitialRun() = runTest {
        setupCleanupActorMocks()

        withDownloadManager(enableCleanupActor = true) {
            advanceTimeBy(CLEANUP_DELAY)
            assertCleanupActorRan(0)
            runCurrent()
            assertCleanupActorRan(1)
            advanceUntilIdle()
            assertCleanupActorRan(1)
        }
    }

    @Test
    fun verifyCleanupActorRunsWhenTriggered() = runTest {
        setupCleanupActorMocks()

        withDownloadManager(enableCleanupActor = true) {
            advanceUntilIdle()
            assertCleanupActorRan(1)
            it.cleanupActor.send(Unit)
            advanceUntilIdle()
            assertCleanupActorRan(2)
        }
    }

    private fun setupCleanupActorMocks() {
        every { dao.get(QUERY_LOCAL_FILES) } returns emptyList()
        every { dao.get(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES) } returns emptyList()
        every { dao.get(QUERY_CLEAN_ORPHANED_LOCAL_FILES) } returns emptyList()
    }

    private suspend fun assertCleanupActorRan(times: Int = 1) {
        if (times > 0) {
            coVerifyOrder {
                repeat(times) {
                    // detectMissingFiles()
                    fs.exists()
                    fs.rootDir()
                    dao.get(QUERY_LOCAL_FILES)

                    // cleanupFilesystem()
                    fs.exists()
                    dao.get(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES)
                    dao.get(QUERY_CLEAN_ORPHANED_LOCAL_FILES)
                    fs.rootDir()
                }
            }
        }
        confirmVerified(dao, fs)
    }

    @Test
    fun verifyDetectMissingFiles() = runTest {
        val file = getTmpFile(true)
        val missingFile = getTmpFile()
        every { dao.get(QUERY_LOCAL_FILES) } returns listOf(LocalFile(file.name), LocalFile(missingFile.name))
        coEvery { fs.rootDir() } returns file.parentFile!!
        coEvery { fs.file(any()) } answers { File(file.parentFile, it.invocation.args[0] as String) }

        withDownloadManager { it.detectMissingFiles() }
        verifyAll {
            dao.get(QUERY_LOCAL_FILES)
            dao.delete(LocalFile(missingFile.name))
        }
    }

    @Test
    fun verifyCleanupFilesystem() = runTest {
        val orphan = spyk(getTmpFile(true))
        val keep = spyk(getTmpFile(true))
        val translation = TranslationFile(1, orphan.name)
        val localFile = LocalFile(orphan.name)
        every { dao.get(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES) } returns listOf(translation)
        every { dao.get(QUERY_CLEAN_ORPHANED_LOCAL_FILES) } returns listOf(localFile)
        keep.name.let {
            every { dao.find<LocalFile>(it) } returns LocalFile(it)
            coEvery { fs.file(it) } returns keep
        }
        orphan.name.let { coEvery { fs.file(it) } returns orphan }

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItem(orphan))
        withDownloadManager { it.cleanFilesystem() }
        assertEquals(setOf(keep), resourcesDir.listFiles()!!.toSet())
        verifyOrder {
            dao.delete(translation)
            dao.delete(localFile)
            orphan.delete()
        }
        verify(inverse = true) { keep.delete() }
    }
    // endregion Cleanup

    private fun getTmpFile(create: Boolean = false) =
        File.createTempFile("test-", null, resourcesDir).also { if (!create) it.delete() }
    private fun getInputStreamForResource(name: String) = this::class.java.getResourceAsStream(name)!!
}
