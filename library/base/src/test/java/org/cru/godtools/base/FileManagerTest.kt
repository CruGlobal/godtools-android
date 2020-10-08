package org.cru.godtools.base

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileManagerTest {
    private lateinit var context: Context
    private val rootDir = File.createTempFile("abc", null).parentFile!!

    private lateinit var fileManager: FileManager

    @Before
    fun setup() {
        context = mock {
            on { filesDir } doReturn rootDir
        }
        fileManager = FileManager(context)
    }

    @Test
    fun verifyCreateResourcesDir() {
        runBlocking {
            assertTrue(fileManager.createResourcesDir())
            assertEquals(File(rootDir, "resources"), fileManager.getResourcesDir())
        }
    }
}
