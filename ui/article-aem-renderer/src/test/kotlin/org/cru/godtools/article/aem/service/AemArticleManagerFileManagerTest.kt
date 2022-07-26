package org.cru.godtools.article.aem.service

import android.net.Uri
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@OptIn(ExperimentalCoroutinesApi::class)
class AemArticleManagerFileManagerTest {
    private val testDir = createTempDirectory().toFile()

    private val resourceDao = mockk<ResourceDao>(relaxUnitFun = true)
    private val fs = spyk(AemFileSystem(mockk { every { filesDir } returns testDir }))

    private val fileManager = AemArticleManager.FileManager(fs, resourceDao)

    @After
    fun cleanup() {
        testDir.deleteRecursively()
    }

    // region storeResponse()
    @Test
    fun `storeResponse()`() = runTest {
        val data = "testWriteToDisk()"
        val uri = mockk<Uri>()
        val resource = Resource(uri)

        val file = fileManager.storeResponse(data.toByteArray().toResponseBody(), resource)
        coVerifyAll { resourceDao.updateLocalFile(uri, any(), any(), any()) }
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file!!.readBytes())
    }

    @Test
    fun `storeResponse() - Dedup`() = runTest {
        val data = "testWriteToDiskDedup()"

        val file1 = fileManager.storeResponse(data.toByteArray().toResponseBody(), Resource(mockk()))!!
        val file2 = fileManager.storeResponse(data.toByteArray().toResponseBody(), Resource(mockk()))!!

        assertEquals(file1, file2)
    }

    @Test
    fun `storeResponse() - No Dedup Without Digest`() = runTest {
        mockkStatic(MessageDigest::class) {
            every { MessageDigest.getInstance(any()) } throws NoSuchAlgorithmException()
            val data = "testWriteToDiskNoDedupWithoutDigest()"

            val file1 = fileManager.storeResponse(data.toByteArray().toResponseBody(), Resource(mockk()))!!
            val file2 = fileManager.storeResponse(data.toByteArray().toResponseBody(), Resource(mockk()))!!

            assertNotEquals(file1, file2)
            assertNotEquals(file1.name, file2.name)
            assertThat(file1.name, startsWith("aem-"))
            assertThat(file2.name, startsWith("aem-"))
        }
    }
    // endregion storeResponse()

    // region removeOrphanedFiles()
    @Test
    fun `removeOrphanedFiles()`() = runTest {
        val validFile = mockk<File>()
        val invalidFile = mockk<File>(relaxed = true)
        coEvery { resourceDao.getAll() } returns listOf(mockk { coEvery { getLocalFile(any()) } returns validFile })
        val rootDir = mockk<File> {
            every { listFiles() } returns arrayOf(validFile, invalidFile)
        }
        coEvery { fs.rootDir() } returns rootDir

        fileManager.removeOrphanedFiles()
        coVerify(exactly = 1) {
            resourceDao.getAll()
            invalidFile.delete()
        }
        coVerify(inverse = true) { validFile.delete() }
        confirmVerified(resourceDao)
    }

    @Test
    fun `removeOrphanedFiles() - Do nothing if FileSystem doesn't exist`() = runTest {
        coEvery { fs.exists() } returns false

        fileManager.removeOrphanedFiles()
        verify { resourceDao wasNot Called }
        coVerify(inverse = true) { fs.rootDir() }
    }
    // endregion removeOrphanedFiles()
}
