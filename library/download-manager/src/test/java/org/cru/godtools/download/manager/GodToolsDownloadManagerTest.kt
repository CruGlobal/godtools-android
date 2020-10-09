package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.nullableArgumentCaptor
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.stubbing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.base.FileManager
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
import retrofit2.Response

private const val TOOL = "tool"

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
    private lateinit var fileManager: FileManager

    private lateinit var downloadManager: KotlinGodToolsDownloadManager

    @Before
    fun setup() {
        attachmentsApi = mock()
        dao = mock()
        eventBus = mock()
        fileManager = mock {
            onBlocking { getResourcesDir() } doReturn resourcesDir
            onBlocking { createResourcesDir() } doReturn true
        }

        downloadManager = KotlinGodToolsDownloadManager(attachmentsApi, dao, eventBus, fileManager)
    }

    @After
    fun cleanup() {
        downloadManager.shutdown()
    }

    // region pinTool()
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
        downloadManager.pinToolAsync(TOOL)
        downloadManager.shutdown()

        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }
    // endregion pinTool()

    // region pinLanguage()
    @Test
    fun verifyPinLanguage() {
        runBlocking { downloadManager.pinLanguage(Locale.FRENCH) }
        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(LanguageUpdateEvent)
    }

    @Test
    fun verifyPinLanguageAsync() {
        downloadManager.pinLanguageAsync(Locale.FRENCH)
        downloadManager.shutdown()

        argumentCaptor<Language> {
            verify(dao).update(capture(), eq(LanguageTable.COLUMN_ADDED))
            assertEquals(Locale.FRENCH, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(LanguageUpdateEvent)
    }
    // endregion pinTool()

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
        val observer: Observer<DownloadProgress?> = mock()
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

            assertEquals(DownloadProgress.INDETERMINATE, firstValue)
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
        filename = "image.jpg"
        sha256 = "sha256"
    }
    private val file = getTmpFile()
    private val testData = Random.nextBytes(16 * 1024)

    @Test
    fun verifyDownloadAttachment() {
        whenever(dao.find<Attachment>(1L)).thenReturn(attachment)
        val response: ResponseBody = mock { on { byteStream() } doReturn testData.inputStream() }
        stubbing(attachmentsApi) { onBlocking { download(any()) } doReturn Response.success(response) }
        whenever(fileManager.getFile(attachment)).thenReturn(file)

        downloadManager.downloadAttachment(1L)
        assertArrayEquals(testData, file.readBytes())
        verify(dao).find<Attachment>(1L)
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyDownloadAttachmentAlreadyDownloaded() {
        attachment.isDownloaded = true
        stubbing(dao) {
            on { find<Attachment>(1L) } doReturn attachment
            on { find<LocalFile>(attachment.localFilename!!) } doReturn attachment.asLocalFile()
        }

        downloadManager.downloadAttachment(1L)
        verify(dao).find<Attachment>(1L)
        verify(dao).find<LocalFile>(attachment.localFilename!!)
        verifyBlocking(attachmentsApi, never()) { download(any()) }
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachment() {
        whenever(fileManager.getFile(attachment)).thenReturn(file)

        testData.inputStream().use { downloadManager.importAttachment(attachment, it) }
        assertArrayEquals(testData, file.readBytes())
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() {
        fileManager.stub {
            onBlocking { createResourcesDir() } doReturn false
            on { getFile(attachment) } doReturn file
        }

        testData.inputStream().use { downloadManager.importAttachment(attachment, it) }
        assertFalse(file.exists())
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(any())
    }

    @Test
    fun verifyImportAttachmentAttachmentAlreadyDownloaded() {
        attachment.isDownloaded = true
        whenever(dao.find<LocalFile>(attachment.localFilename!!)).thenReturn(attachment.asLocalFile())
        whenever(fileManager.getFile(attachment)).thenReturn(file)

        testData.inputStream().use { downloadManager.importAttachment(attachment, it) }
        verify(dao, never()).updateOrInsert(any())
        verify(dao, never()).update(any(), anyVararg<String>())
        verify(eventBus, never()).post(any())
        verify(fileManager, never()).getFile(any())
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentLocalFileExists() {
        attachment.isDownloaded = false
        whenever(fileManager.getFile(attachment)).thenReturn(file)
        whenever(dao.find<LocalFile>(attachment.localFilename!!)).thenReturn(attachment.asLocalFile())

        testData.inputStream().use { downloadManager.importAttachment(attachment, it) }
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        verify(fileManager, never()).getFile(any())
        verify(dao, never()).updateOrInsert(any())
        assertTrue(attachment.isDownloaded)
    }

    private fun Attachment.asLocalFile() = LocalFile(localFilename!!)
    private fun FileManager.getFile(attachment: Attachment) = getFile(attachment.localFilename)
    // endregion Attachments

    // region Translations
    private val translation = Translation().apply {
        setId(Random.nextLong())
        toolCode = TOOL
        languageCode = Locale.FRENCH
    }

    @Test
    fun verifyStoreTranslation() {
        val files = Array(3) { getTmpFile() }
        fileManager.stub {
            on { getFile("a.txt") } doReturn files[0]
            on { getFile("b.txt") } doReturn files[1]
            on { getFile("c.txt") } doReturn files[2]
        }

        downloadManager.storeTranslation(translation, getInputStreamForResource("abc.zip"), -1)
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
    }
    // endregion Translations

    // region Cleanup
    @Test
    fun verifyDetectMissingFiles() {
        val file = getTmpFile(true)
        val missingFile = getTmpFile()
        whenever(dao.get(any<Query<LocalFile>>())).thenReturn(listOf(LocalFile(file.name), LocalFile(missingFile.name)))
        fileManager.stub {
            onBlocking { getResourcesDir() } doReturn file.parentFile!!
            on { getFile(any()) } doAnswer { File(file.parentFile, it.getArgument(0)) }
        }

        downloadManager.detectMissingFiles()
        verify(dao, never()).delete(LocalFile(file.name))
        verify(dao).delete(LocalFile(missingFile.name))
    }

    @Test
    fun verifyCleanupFilesystem() {
        val orphan = getTmpFile(true)
        val translation = TranslationFile(1, orphan.name)
        val localFile = LocalFile(orphan.name)
        val keep = getTmpFile(true)
        dao.stub {
            on { get(argThat<Query<*>> { table.type == TranslationFile::class.java }) } doReturn listOf(translation)
            on { get(argThat<Query<*>> { table.type == LocalFile::class.java }) } doReturn listOf(localFile)
            on { find<LocalFile>(keep.name) } doReturn LocalFile(keep.name)
        }

        assertThat(resourcesDir.listFiles()!!.toSet(), hasItem(orphan))
        downloadManager.cleanFilesystem()
        verify(dao).delete(translation)
        verify(dao).delete(localFile)
        assertEquals(setOf(keep), resourcesDir.listFiles()!!.toSet())
    }
    // endregion Cleanup

    private fun getTmpFile(create: Boolean = false) =
        File.createTempFile("test-", null, resourcesDir).also { if (!create) it.delete() }
    private fun getInputStreamForResource(name: String) = this::class.java.getResourceAsStream(name)!!
}
