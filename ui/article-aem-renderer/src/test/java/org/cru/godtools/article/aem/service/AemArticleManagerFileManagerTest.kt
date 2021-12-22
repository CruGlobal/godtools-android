package org.cru.godtools.article.aem.service

import java.io.File
import kotlinx.coroutines.runBlocking
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileSystem
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class AemArticleManagerFileManagerTest {
    private val resourceDao = mock<ResourceDao>()
    private val fs = mock<AemFileSystem> {
        onBlocking { exists() } doReturn true
        onBlocking { rootDir() } doReturn mock()
    }

    private val fileManager = AemArticleManager.FileManager(fs, resourceDao)

    // region removeOrphanedFiles()
    @Test
    fun `removeOrphanedFiles()`(): Unit = runBlocking {
        val validFile = mock<File>()
        val invalidFile = mock<File>()
        val resources = listOf<Resource>(mock { onBlocking { getLocalFile(any()) } doReturn validFile })
        whenever(resourceDao.getAll()) doReturn resources
        whenever(fs.rootDir().listFiles()) doReturn arrayOf(validFile, invalidFile)

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
