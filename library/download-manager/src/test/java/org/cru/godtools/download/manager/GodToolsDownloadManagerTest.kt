package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.argumentCaptor
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
import org.ccci.gto.android.common.db.find
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.base.FileManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.greenrobot.eventbus.EventBus
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
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import retrofit2.Response

private const val TOOL = "tool"

class GodToolsDownloadManagerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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
        fileManager = mock { on { createResourcesDir() } doReturn true }

        downloadManager = KotlinGodToolsDownloadManager(attachmentsApi, dao, eventBus, fileManager)
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

        runBlocking {
            with(downloadManager.job) {
                complete()
                join()
            }
        }
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
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
    private val file = File.createTempFile("test-", null).also { it.delete() }
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

        downloadManager.importAttachment(attachment, testData.inputStream())
        assertArrayEquals(testData, file.readBytes())
        verify(dao).updateOrInsert(eq(attachment.asLocalFile()))
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        assertTrue(attachment.isDownloaded)
    }

    @Test
    fun verifyImportAttachmentUnableToCreateResourcesDir() {
        fileManager.stub {
            on { createResourcesDir() } doReturn false
            on { getFile(attachment) } doReturn file
        }

        downloadManager.importAttachment(attachment, testData.inputStream())
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

        downloadManager.importAttachment(attachment, testData.inputStream())
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

        downloadManager.importAttachment(attachment, testData.inputStream())
        verify(dao).update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
        verify(eventBus).post(AttachmentUpdateEvent)
        verify(fileManager, never()).getFile(any())
        verify(dao, never()).updateOrInsert(any())
        assertTrue(attachment.isDownloaded)
    }

    // TODO: temporary test, remove once we test the public entry points
    @Test
    fun verifyCopyToLocalFile() {
        val file = File.createTempFile("test-", null)
        val localFile: LocalFile = mock {
            on { getFile(eq(fileManager)) } doReturn file
        }
        val testData = Random.nextBytes(16 * 1024)

        with(downloadManager) { testData.inputStream().copyTo(localFile) }
        assertArrayEquals(testData, file.readBytes())
    }

    private fun Attachment.asLocalFile() = LocalFile(localFilename!!)
    private fun FileManager.getFile(attachment: Attachment) = getFile(attachment.localFilename)
    // endregion Attachments
}
