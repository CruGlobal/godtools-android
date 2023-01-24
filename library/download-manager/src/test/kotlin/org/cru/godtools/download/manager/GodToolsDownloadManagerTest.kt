package org.cru.godtools.download.manager

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.cash.turbine.test
import app.cash.turbine.testIn
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coExcludeRecords
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
import io.mockk.verifyOrder
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import okio.buffer
import okio.source
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.api.TranslationsApi
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserConfig
import org.cru.godtools.shared.tool.parser.ParserResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository
import retrofit2.Response

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("BlockingMethodInNonBlockingContext")
class GodToolsDownloadManagerTest {
    private val resourcesDir = File.createTempFile("resources", "").also {
        it.delete()
        it.mkdirs()
    }

    private val attachmentsApi = mockk<AttachmentsApi>()
    private val attachmentsRepository: AttachmentsRepository = mockk(relaxUnitFun = true)
    private val dao = mockk<GodToolsDao>(relaxUnitFun = true) {
        every { deleteAsync(any()) } returns CompletableDeferred(Unit)
        every { transaction(any(), any<() -> Any>()) } answers { (it.invocation.args[1] as () -> Any).invoke() }
        excludeRecords { transaction(any(), any()) }
    }
    private val downloadedFilesRepository: DownloadedFilesRepository = mockk(relaxUnitFun = true) {
        coEvery { findDownloadedFile(any()) } returns null
        coEvery { getDownloadedFiles() } returns emptyList()
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
    private val translationsApi = mockk<TranslationsApi>()
    private val translationsRepository = mockk<TranslationsRepository>()
    private val workManager = mockk<WorkManager> {
        every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
    }
    private val testScope = TestScope()

    private val downloadManager = GodToolsDownloadManager(
        attachmentsApi,
        attachmentsRepository,
        dao,
        downloadedFilesRepository,
        fs,
        manifestParser,
        translationsApi,
        translationsRepository,
        { workManager },
        testScope.backgroundScope,
        UnconfinedTestDispatcher(testScope.testScheduler)
    )

    // region Download Progress
    @Test
    fun verifyDownloadProgressFlow() = testScope.runTest {
        val translationKey = TranslationKey(TOOL, Locale.ENGLISH)
        downloadManager.getDownloadProgressFlow(TOOL, Locale.ENGLISH).test {
            assertNull(awaitItem())

            // start download
            downloadManager.startProgress(translationKey)
            assertSame(DownloadProgress.INITIAL, awaitItem())

            // update progress
            downloadManager.updateProgress(translationKey, 3, 5)
            assertEquals(DownloadProgress(3, 5), awaitItem())
            downloadManager.updateProgress(translationKey, 5, 10)
            assertEquals(DownloadProgress(5, 10), awaitItem())

            // finish download
            downloadManager.finishDownload(translationKey)
            assertNull(awaitItem())
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
        coEvery { attachmentsRepository.findAttachment(attachment.id) } returns attachment
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) }
            .returns(attachment.asDownloadedFile())

        coExcludeRecords {
            attachmentsRepository.findAttachment(attachment.id)
            downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!)
        }
    }

    // region downloadAttachment()
    @Test
    fun `downloadAttachment()`() = testScope.runTest {
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) } returns null
        val response = RealResponseBody(null, 0, testData.inputStream().source().buffer())
        coEvery { attachmentsApi.download(any()) } returns Response.success(response)
        coEvery { attachment.getFile(fs) } returns file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            downloadedFilesRepository.insertOrIgnore(attachment.asDownloadedFile())
            attachmentsRepository.updateAttachmentDownloaded(attachment.id, true)
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded`() = testScope.runTest {
        attachment.isDownloaded = true

        downloadManager.downloadAttachment(attachment.id)
        assertTrue(attachment.isDownloaded)
        verify { attachmentsApi wasNot Called }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing`() = testScope.runTest {
        attachment.isDownloaded = true
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) } returns null
        val response = RealResponseBody(null, 0, testData.inputStream().source().buffer())
        coEvery { attachmentsApi.download(attachment.id) } returns Response.success(response)
        coEvery { attachment.getFile(fs) } returns file

        downloadManager.downloadAttachment(attachment.id)
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            downloadedFilesRepository.insertOrIgnore(attachment.asDownloadedFile())
            attachmentsRepository.updateAttachmentDownloaded(attachment.id, true)
        }
    }

    @Test
    fun `downloadAttachment() - Already Downloaded, LocalFile missing, fails download`() = testScope.runTest {
        attachment.isDownloaded = true
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) } returns null
        coEvery { attachmentsApi.download(attachment.id) } throws IOException()
        coEvery { attachment.getFile(fs) } returns file

        downloadManager.downloadAttachment(attachment.id)
        assertFalse(file.exists())
        assertFalse(attachment.isDownloaded)
        verify(inverse = true) { downloadedFilesRepository.insertOrIgnore(attachment.asDownloadedFile()) }
        coVerifySequence {
            attachmentsApi.download(attachment.id)
            attachmentsRepository.updateAttachmentDownloaded(attachment.id, false)
        }
    }

    @Test
    fun `downloadAttachment() - Download fails`() = testScope.runTest {
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) } returns null
        coEvery { attachmentsApi.download(attachment.id) } throws IOException()

        downloadManager.downloadAttachment(attachment.id)
        assertFalse(attachment.isDownloaded)
        coVerifySequence { attachmentsApi.download(attachment.id) }
    }
    // endregion downloadAttachment()

    // region importAttachment()
    @Test
    fun verifyImportAttachment() = testScope.runTest {
        coEvery { downloadedFilesRepository.findDownloadedFile(attachment.localFilename!!) } returns null
        coEvery { attachment.getFile(fs) } returns file

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertArrayEquals(testData, file.readBytes())
        assertTrue(attachment.isDownloaded)
        coVerifySequence {
            downloadedFilesRepository.insertOrIgnore(attachment.asDownloadedFile())
            attachmentsRepository.updateAttachmentDownloaded(attachment.id, true)
        }
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() = testScope.runTest {
        coEvery { fs.exists() } returns false
        coEvery { attachment.getFile(fs) } returns file

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertFalse(file.exists())
        verify { dao wasNot Called }
    }

    @Test
    fun verifyImportAttachmentAttachmentAlreadyDownloaded() = testScope.runTest {
        attachment.isDownloaded = true

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertTrue(attachment.isDownloaded)
        coVerify(inverse = true) {
            fs.file(any())
            dao.updateOrInsert(any())
            dao.update(any())
        }
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() = testScope.runTest {
        attachment.isDownloaded = false

        testData.inputStream().use { downloadManager.importAttachment(attachment.id, it) }
        assertTrue(attachment.isDownloaded)
        coVerify {
            attachmentsRepository.updateAttachmentDownloaded(attachment.id, true)
            dao wasNot Called
        }
        coVerify(exactly = 0) { fs.file(any()) }
    }
    // endregion importAttachment()

    private fun Attachment.asDownloadedFile() = DownloadedFile(localFilename!!)
    // endregion Attachments

    // region Translations
    private val translation = Translation().apply {
        id = Random.nextLong()
        toolCode = TOOL
        languageCode = Locale.FRENCH
        isDownloaded = false
    }

    @Before
    fun setupTranslationMocks() {
        every { dao.update(any<Translation>(), TranslationTable.COLUMN_DOWNLOADED) } returns 1
    }

    // region downloadLatestPublishedTranslation()
    @Test
    fun `downloadLatestPublishedTranslation() - Files`() = testScope.runTest {
        downloadManager.cleanupActor.close()
        translation.manifestFileName = "manifest.xml"
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
        val config = slot<ParserConfig>()
        coEvery { manifestParser.parseManifest("manifest.xml", capture(config)) } returns
            ParserResult.Data(mockk { every { relatedFiles } returns setOf("a.txt", "b.txt") })
        coEvery { translationsApi.downloadFile("manifest.xml") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("manifest")))
        coEvery { translationsApi.downloadFile("a.txt") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("a".repeat(1024))))
        coEvery { translationsApi.downloadFile("b.txt") } returns
            Response.success(RealResponseBody(null, 0, Buffer().writeUtf8("b".repeat(1024))))
        val progressFlow = downloadManager.getDownloadProgressFlow(TOOL, Locale.FRENCH).testIn(this)
        assertNull(progressFlow.awaitItem())

        // HACK: suppress dao calls from pruneTranslations()
        every { dao.get(QUERY_STALE_TRANSLATIONS) } returns emptyList()
        excludeRecords { dao.get(QUERY_STALE_TRANSLATIONS) }

        assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        assertTrue(translation.isDownloaded)
        assertEquals(setOf("manifest.xml", "a.txt", "b.txt"), files.keys)
        assertArrayEquals("manifest".toByteArray(), files["manifest.xml"]!!.readBytes())
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertEquals(config.captured.withParseRelated(false), config.captured)
        coVerifyAll {
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
            downloadedFilesRepository.findDownloadedFile("manifest.xml")
            translationsApi.downloadFile("manifest.xml")
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("manifest.xml"))
            manifestParser.parseManifest("manifest.xml", any())

            downloadedFilesRepository.findDownloadedFile("a.txt")
            downloadedFilesRepository.findDownloadedFile("b.txt")
            translationsApi.downloadFile("a.txt")
            translationsApi.downloadFile("b.txt")
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("a.txt"))
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "manifest.xml"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
            workManager wasNot Called
        }
        assertSame(DownloadProgress.INITIAL, progressFlow.awaitItem())
        assertNull(progressFlow.expectMostRecentItem())
        progressFlow.cancel()
    }

    @Test
    fun `downloadLatestPublishedTranslation() - Zip`() = testScope.runTest {
        downloadManager.cleanupActor.close()
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
        val response = RealResponseBody(null, 0, getInputStreamForResource("abc.zip").source().buffer())
        coEvery { translationsApi.download(translation.id) } returns Response.success(response)
        val progressFlow = downloadManager.getDownloadProgressFlow(TOOL, Locale.FRENCH).testIn(this)
        assertNull(progressFlow.awaitItem())

        // HACK: suppress dao calls from pruneTranslations()
        every { dao.get(QUERY_STALE_TRANSLATIONS) } returns emptyList()
        excludeRecords { dao.get(QUERY_STALE_TRANSLATIONS) }

        assertTrue(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        assertTrue(translation.isDownloaded)
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files["c.txt"]!!.readBytes())

        coVerifyAll {
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
            translationsApi.download(translation.id)
            downloadedFilesRepository.findDownloadedFile("a.txt")
            downloadedFilesRepository.findDownloadedFile("b.txt")
            downloadedFilesRepository.findDownloadedFile("c.txt")
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("a.txt"))
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("b.txt"))
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("c.txt"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "c.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
            workManager wasNot Called
        }
        assertSame(DownloadProgress.INITIAL, progressFlow.awaitItem())
        assertNull(progressFlow.expectMostRecentItem())
        progressFlow.cancel()
    }

    @Test
    fun `downloadLatestPublishedTranslation() - Zip - API IOException`() = testScope.runTest {
        coEvery {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode)
        } returns translation
        coEvery { translationsApi.download(translation.id) } throws IOException()
        val progressFlow = downloadManager.getDownloadProgressFlow(TOOL, Locale.FRENCH).testIn(this)
        assertNull(progressFlow.awaitItem())

        assertFalse(downloadManager.downloadLatestPublishedTranslation(TranslationKey(translation)))
        coVerifyAll {
            translationsRepository.getLatestTranslation(TOOL, Locale.FRENCH)
            translationsApi.download(translation.id)
            workManager.enqueueUniqueWork(any(), ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
        }
        assertSame(DownloadProgress.INITIAL, progressFlow.awaitItem())
        assertNull(progressFlow.expectMostRecentItem())
        progressFlow.cancel()
    }
    // endregion downloadLatestPublishedTranslation()

    @Test
    fun verifyImportTranslation() = testScope.runTest {
        coEvery { translationsRepository.getLatestTranslation(any(), any(), any()) } returns null
        val progressFlow = downloadManager.getDownloadProgressFlow(TOOL, Locale.FRENCH).testIn(this)
        assertNull(progressFlow.awaitItem())

        downloadManager.importTranslation(translation, getInputStreamForResource("abc.zip"), -1)
        assertArrayEquals("a".repeat(1024).toByteArray(), files["a.txt"]!!.readBytes())
        assertArrayEquals("b".repeat(1024).toByteArray(), files["b.txt"]!!.readBytes())
        assertArrayEquals("c".repeat(1024).toByteArray(), files["c.txt"]!!.readBytes())
        coVerifyAll {
            translationsRepository.getLatestTranslation(translation.toolCode, translation.languageCode, true)
            downloadedFilesRepository.findDownloadedFile("a.txt")
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("a.txt"))
            dao.updateOrInsert(TranslationFile(translation, "a.txt"))
            downloadedFilesRepository.findDownloadedFile("b.txt")
            downloadedFilesRepository.findDownloadedFile("c.txt")
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("b.txt"))
            downloadedFilesRepository.insertOrIgnore(DownloadedFile("c.txt"))
            dao.updateOrInsert(TranslationFile(translation, "b.txt"))
            dao.updateOrInsert(TranslationFile(translation, "c.txt"))
            dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
        }
        assertSame(DownloadProgress.INITIAL, progressFlow.awaitItem())
        assertNull(progressFlow.expectMostRecentItem())
        progressFlow.cancel()
    }

    @Test
    fun verifyPruneStaleTranslations() = testScope.runTest {
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

        downloadManager.pruneStaleTranslations()
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
    fun verifyCleanupActorInitialRun() = testScope.runTest {
        setupCleanupActorMocks()

        advanceTimeBy(CLEANUP_DELAY)
        assertCleanupActorRan(0)
        runCurrent()
        assertCleanupActorRan(1)
        advanceTimeBy(50 * CLEANUP_DELAY)
        assertCleanupActorRan(1)
    }

    @Test
    fun verifyCleanupActorRunsOnceWhenTriggered() = testScope.runTest {
        setupCleanupActorMocks()

        advanceTimeBy(50 * CLEANUP_DELAY)
        assertCleanupActorRan(1)
        downloadManager.cleanupActor.send(Unit)
        advanceTimeBy(CLEANUP_DELAY)
        assertCleanupActorRan(1)
        runCurrent()
        assertCleanupActorRan(2)
        advanceTimeBy(50 * CLEANUP_DELAY)
        assertCleanupActorRan(2)
    }

    private fun setupCleanupActorMocks() {
        every { dao.getAsync(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES) } returns CompletableDeferred(emptyList())
        coEvery { attachmentsRepository.getAttachments() } returns emptyList()
        every { dao.get(TranslationFile::class.java) } returns emptyList()
    }

    private suspend fun assertCleanupActorRan(times: Int = 1) {
        if (times > 0) {
            coVerifyOrder {
                repeat(times) {
                    fs.exists()

                    // detectMissingFiles()
                    fs.rootDir()
                    downloadedFilesRepository.getDownloadedFiles()

                    // deleteOrphanedTranslationFiles()
                    dao.getAsync(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES)

                    // deleteUnusedDownloadedFiles()
                    attachmentsRepository.getAttachments()
                    dao.get(TranslationFile::class.java)
                    downloadedFilesRepository.getDownloadedFiles()

                    // deleteOrphanedFiles()
                    fs.rootDir()
                }
            }
        }
        confirmVerified(dao, fs)
    }

    @Test
    fun verifyDetectMissingFiles() = testScope.runTest {
        val file = getTmpFile(true)
        val missingFile = getTmpFile()
        val downloadedFiles = listOf(DownloadedFile(file.name), DownloadedFile(missingFile.name))
        coEvery { downloadedFilesRepository.getDownloadedFiles() } returns downloadedFiles
        coEvery { fs.rootDir() } returns file.parentFile!!
        coEvery { fs.file(any()) } answers { File(file.parentFile, it.invocation.args[0] as String) }

        downloadManager.detectMissingFiles()
        coVerifyAll {
            downloadedFilesRepository.getDownloadedFiles()
            dao.delete(DownloadedFile(missingFile.name))
        }
    }

    @Test
    fun `deleteOrphanedTranslationFiles()`() = testScope.runTest {
        val file = TranslationFile(1, "file")
        every { dao.getAsync(QUERY_CLEAN_ORPHANED_TRANSLATION_FILES) } returns CompletableDeferred(listOf(file))

        downloadManager.deleteOrphanedTranslationFiles()
        verify { dao.deleteAsync(file) }
    }

    // region deleteUnusedDownloadedFiles()
    @Test
    fun `deleteUnusedDownloadedFiles()`() = testScope.runTest {
        val file = spyk(getTmpFile(true))
        val fileName = file.name
        coEvery { fs.file(fileName) } returns file
        coEvery { attachmentsRepository.getAttachments() } returns emptyList()
        every { dao.get(TranslationFile::class.java) } returns emptyList()
        coEvery { downloadedFilesRepository.getDownloadedFiles() } returns listOf(DownloadedFile(fileName))

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItems(file))
        downloadManager.deleteUnusedDownloadedFiles()
        assertThat(resourcesDir.listFiles()!!.toSet(), not(hasItems(file)))
        verifyOrder {
            downloadedFilesRepository.delete(DownloadedFile(fileName))
            file.delete()
        }
    }

    @Test
    fun `deleteUnusedDownloadedFiles() - keep downloaded attachments`() = testScope.runTest {
        val keep = spyk(getTmpFile(suffix = ".bin", create = true))
        val remove = spyk(getTmpFile(suffix = ".bin", create = true))
        val removeName = remove.name
        coEvery { attachmentsRepository.getAttachments() } returns listOf(
            Attachment().apply {
                filename = keep.name
                sha256 = keep.name.substringBeforeLast(".")
                isDownloaded = true
            },
            Attachment().apply {
                filename = remove.name
                sha256 = remove.name.substringBeforeLast(".")
                isDownloaded = false
            }
        )
        every { dao.get(TranslationFile::class.java) } returns emptyList()
        coEvery { downloadedFilesRepository.getDownloadedFiles() } returns listOf(
            DownloadedFile(keep.name),
            DownloadedFile(remove.name),
        )

        coEvery { fs.file(removeName) } returns remove

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItems(keep, remove))
        downloadManager.deleteUnusedDownloadedFiles()
        assertThat(resourcesDir.listFiles()!!.toSet(), allOf(hasItem(keep), not(hasItem(remove))))
        verifyOrder {
            downloadedFilesRepository.delete(DownloadedFile(removeName))
            remove.delete()
        }
        verify(exactly = 0) { keep.delete() }
    }
    // endregion deleteUnusedDownloadedFiles()

    // region deleteOrphanedFiles()
    @Test
    fun `deleteOrphanedFiles()`() = testScope.runTest {
        val keep = getTmpFile(create = true)
        val orphan = getTmpFile(create = true)
        coEvery { downloadedFilesRepository.findDownloadedFile(keep.name) } returns DownloadedFile(keep.name)

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItems(keep, orphan))
        downloadManager.deleteOrphanedFiles()
        assertThat(resourcesDir.listFiles()!!.toSet(), allOf(hasItem(keep), not(hasItem(orphan))))
    }
    // endregion deleteOrphanedFiles()
    // endregion Cleanup

    private fun getTmpFile(create: Boolean = false, suffix: String? = null) =
        File.createTempFile("test-", suffix, resourcesDir).also { if (!create) it.delete() }
    private fun getInputStreamForResource(name: String) = this::class.java.getResourceAsStream(name)!!
}
