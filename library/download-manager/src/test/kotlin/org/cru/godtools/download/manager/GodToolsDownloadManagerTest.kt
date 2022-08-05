package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
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
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.tool.ParserConfig
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.service.ParserResult
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
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.ToolsRepository
import org.keynote.godtools.android.db.repository.TranslationsRepository
import retrofit2.Response

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("BlockingMethodInNonBlockingContext")
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
    private val files = mutableMapOf<String, File>()
    private val fs = mockk<ToolFileSystem> {
        coEvery { rootDir() } returns resourcesDir
        coEvery { exists() } returns true
        coEvery { file(any()) } answers { files.getOrPut(it.invocation.args[0] as String) { getTmpFile() } }
    }
    private val manifestParser = mockk<ManifestParser> {
        every { defaultConfig } returns ParserConfig()
        excludeRecords { defaultConfig }
    }
    private val settings = mockk<Settings> {
        every { isLanguageProtected(any()) } returns false
        every { parallelLanguage } returns null
    }
    private val toolsRepository = mockk<ToolsRepository>(relaxUnitFun = true)
    private val translationsApi = mockk<TranslationsApi>()
    private val translationsRepository = mockk<TranslationsRepository>()
    private val workManager = mockk<WorkManager> {
        every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
    }

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
        Dispatchers.setMain(dispatcher)
        val downloadManager = GodToolsDownloadManager(
            attachmentsApi,
            dao,
            fs,
            manifestParser,
            settings,
            toolsRepository,
            translationsApi,
            translationsRepository,
            { workManager },
            this,
            dispatcher
        )
        try {
            if (!enableCleanupActor) downloadManager.cleanupActor.close()
            block(downloadManager)
        } finally {
            downloadManager.cleanupActor.close()
            Dispatchers.resetMain()
        }
    }

    // region pinTool()/unpinTool()
    private val tool = slot<String>()

    @Test
    fun verifyPinToolAsync() = runTest {
        coEvery { toolsRepository.pinTool(capture(tool)) } just Runs

        withDownloadManager { it.pinToolAsync(TOOL).join() }
        assertEquals(TOOL, tool.captured)
        coVerifyAll { toolsRepository.pinTool(tool.captured) }
    }

    @Test
    fun verifyUnpinToolAsync() = runTest {
        coEvery { toolsRepository.unpinTool(capture(tool)) } just Runs

        withDownloadManager { it.unpinToolAsync(TOOL).join() }
        assertEquals(TOOL, tool.captured)
        coVerifyAll { toolsRepository.unpinTool(tool.captured) }
    }
    // endregion pinTool()/unpinTool()

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
            verify(exactly = 1) { observer.onChanged(any()) }
            assertNull(downloadProgress.removeAt(0))

            // start download
            downloadManager.startProgress(translationKey)
            verify(exactly = 2) { observer.onChanged(any()) }
            assertSame(DownloadProgress.INITIAL, downloadProgress.removeAt(0))

            // update progress
            downloadManager.updateProgress(translationKey, 3, 5)
            downloadManager.updateProgress(translationKey, 5, 10)
            verify(exactly = 4) { observer.onChanged(any()) }
            assertEquals(DownloadProgress(3, 5), downloadProgress.removeAt(0))
            assertEquals(DownloadProgress(5, 10), downloadProgress.removeAt(0))

            // finish download
            downloadManager.finishDownload(translationKey)
            verify(exactly = 5) { observer.onChanged(any()) }
            assertNull(downloadProgress.removeAt(0))
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
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded`() = runTest {
        attachment.isDownloaded = true

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertTrue(attachment.isDownloaded)
        verify { attachmentsApi wasNot Called }
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
        }
    }

    @Test
    fun `downloadAttachment() - Download fails`() = runTest {
        every { dao.find<LocalFile>(attachment.localFilename!!) } returns null
        coEvery { attachmentsApi.download(attachment.id) } throws IOException()

        withDownloadManager { it.downloadAttachment(attachment.id) }
        assertFalse(attachment.isDownloaded)
        coVerifySequence { attachmentsApi.download(attachment.id) }
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
        verify { dao wasNot Called }
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
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() = runTest {
        attachment.isDownloaded = false

        withDownloadManager { downloadManager ->
            testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        }
        assertTrue(attachment.isDownloaded)
        verify { dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED) }
        coVerify(inverse = true) {
            fs.file(any())
            dao.updateOrInsert(any())
        }
        confirmVerified(dao)
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
    fun `downloadLatestPublishedTranslation() - Files`() = runTest {
        translation.manifestFileName = "manifest.xml"
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
        every { dao.find<LocalFile>(any<String>()) } returns null
        val config = slot<ParserConfig>()
        coEvery { manifestParser.parseManifest("manifest.xml", capture(config)) } returns
            ParserResult.Data(mockk { every { relatedFiles } returns setOf("a.txt", "b.txt") })
        coEvery { translationsApi.downloadFile("manifest.xml") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("manifest")))
        coEvery { translationsApi.downloadFile("a.txt") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("a".repeat(1024))))
        coEvery { translationsApi.downloadFile("b.txt") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("b".repeat(1024))))

        // HACK: suppress dao calls from pruneTranslations()
        every { dao.get(QUERY_STALE_TRANSLATIONS) } returns emptyList()
        excludeRecords { dao.get(QUERY_STALE_TRANSLATIONS) }

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        }
        assertTrue(translation.isDownloaded)
        assertEquals(setOf("manifest.xml", "a.txt", "b.txt"), files.keys)
        assertArrayEquals("manifest".toByteArray(), files["manifest.xml"]!!.readBytes())
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertEquals(config.captured.withParseRelated(false), config.captured)

        coVerifyAll {
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
            dao.find<LocalFile>("manifest.xml")
            translationsApi.downloadFile("manifest.xml")
            dao.updateOrInsert(LocalFile("manifest.xml"))
            manifestParser.parseManifest("manifest.xml", any())

            dao.find<LocalFile>("a.txt")
            dao.find<LocalFile>("b.txt")
            translationsApi.downloadFile("a.txt")
            translationsApi.downloadFile("b.txt")
            dao.updateOrInsert(LocalFile("a.txt"))
            dao.updateOrInsert(LocalFile("b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "manifest.xml"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
            workManager wasNot Called
            observer.onChanged(any())
        }
        assertNull(downloadProgress.last())
    }

    @Test
    fun `downloadLatestPublishedTranslation() - Zip`() = runTest {
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
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
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
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
            workManager wasNot Called
            observer.onChanged(any())
        }
        assertNull(downloadProgress.last())
    }

    @Test
    fun `downloadLatestPublishedTranslation() - Zip - API IOException`() = runTest {
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
        coEvery { translationsApi.download(translation.id) } throws IOException()

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            assertFalse(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        }
        assertNull(downloadProgress.last())
        coVerifyAll {
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
            translationsApi.download(translation.id)
            workManager.enqueueUniqueWork(any(), ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
            observer.onChanged(any())
        }
    }
    // endregion downloadLatestPublishedTranslation()

    @Test
    fun verifyImportTranslation() = runTest {
        coEvery { translationsRepository.getLatestTranslation(any(), any(), any()) } returns null
        every { dao.find<LocalFile>(any<String>()) } returns null

        withDownloadManager { downloadManager ->
            downloadManager.getDownloadProgressLiveData(TOOL, Locale.FRENCH).observeForever(observer)
            downloadManager.importTranslation(translation, getInputStreamForResource("abc.zip"), -1)
        }
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files["c.txt"]!!.readBytes())
        coVerifyAll {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode, true)
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
        verify { dao.update(invalid, TranslationTable.COLUMN_DOWNLOADED) }
        verify(inverse = true) {
            dao.update(valid1, TranslationTable.COLUMN_DOWNLOADED)
            dao.update(valid2, TranslationTable.COLUMN_DOWNLOADED)
            dao.update(valid3, TranslationTable.COLUMN_DOWNLOADED)
        }
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
