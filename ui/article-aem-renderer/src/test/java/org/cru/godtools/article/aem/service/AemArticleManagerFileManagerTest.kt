package org.cru.godtools.article.aem.service

import android.net.Uri
import java.io.File
import java.security.MessageDigest
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileSystem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class AemArticleManagerFileManagerTest {
    private val testDir = createTempDirectory().toFile()

    private val resourceDao = mock<ResourceDao>()
    private val fs = spy(AemFileSystem(mock { on { filesDir } doReturn testDir }))

    private val fileManager = AemArticleManager.FileManager(fs, resourceDao)

    @After
    fun cleanup() {
        testDir.deleteRecursively()
    }

    // region storeResponse()
    @Test
    fun `storeResponse()`() = runBlocking {
        val data = "testWriteToDisk()"
        val uri = mock<Uri>()
        val resource = Resource(uri)

        val file = fileManager.storeResponse(data.toByteArray().toResponseBody(), resource)
        verify(resourceDao).updateLocalFile(eq(uri), anyOrNull(), any(), any())
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file!!.readBytes())
    }

    @Test
    fun `storeResponse() - Dedup`() = runBlocking {
        val data = "testWriteToDiskDedup()"

        val file1 = fileManager.storeResponse(data.toByteArray().toResponseBody(), mock())!!
        val file2 = fileManager.storeResponse(data.toByteArray().toResponseBody(), mock())!!

        assertEquals(file1, file2)
    }

    @Test
    fun `storeResponse() - No Dedup Without Digest`() = runBlocking {
        mockStatic(MessageDigest::class.java).use {
            it.`when`<MessageDigest?> { MessageDigest.getInstance("SHA-1") } doReturn null
            val data = "testWriteToDiskNoDedupWithoutDigest()"

            val file1 = fileManager.storeResponse(data.toByteArray().toResponseBody(), mock())!!
            val file2 = fileManager.storeResponse(data.toByteArray().toResponseBody(), mock())!!

            assertNotEquals(file1, file2)
            assertNotEquals(file1.name, file2.name)
            assertThat(file1.name, startsWith("aem-"))
            assertThat(file2.name, startsWith("aem-"))
        }
    }
    // endregion storeResponse()

    // region removeOrphanedFiles()
    @Test
    fun `removeOrphanedFiles()`(): Unit = runBlocking {
        val validFile = mock<File>()
        val invalidFile = mock<File>()
        val resources = listOf<Resource>(mock { onBlocking { getLocalFile(any()) } doReturn validFile })
        whenever(resourceDao.getAll()) doReturn resources
        val rootDir = mock<File> {
            on { listFiles() } doReturn arrayOf(validFile, invalidFile)
        }
        whenever(fs.rootDir()) doReturn rootDir

        fileManager.removeOrphanedFiles()
        verify(resourceDao).getAll()
        verify(validFile, never()).delete()
        verify(invalidFile).delete()
    }

    @Test
    fun `removeOrphanedFiles() - Do nothing if FileSystem doesn't exist`(): Unit = runBlocking {
        whenever(fs.exists()) doReturn false

        fileManager.removeOrphanedFiles()
        verifyNoInteractions(resourceDao)
        verify(fs, never()).rootDir()
    }
    // endregion removeOrphanedFiles()
}
