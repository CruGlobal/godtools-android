package org.cru.godtools.base

import android.content.Context
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class FileManagerTest {
    private lateinit var context: Context
    private val rootDir = File.createTempFile("abc", null).parentFile!!

    private lateinit var fileManager: FileManager

    @Before
    fun setup() {
        context = mock {
            on { filesDir } doReturn rootDir
        }
        fileManager = object : FileManager(context, "resources") {}
    }

    @Test
    fun testCreateDir() {
        runBlocking {
            assertTrue(fileManager.createDir())
            assertEquals(File(rootDir, "resources"), fileManager.getDir())
        }
    }
}
