package org.cru.godtools.base

import android.content.Context
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FileSystemTest {
    private lateinit var context: Context
    private val rootDir = File.createTempFile("abc", null).parentFile!!

    private lateinit var fileSystem: FileSystem

    @Before
    fun setup() {
        context = mock {
            on { filesDir } doReturn rootDir
        }
        fileSystem = FileSystem(context, "resources")
    }

    @Test
    fun testExists() = runTest {
        assertTrue(fileSystem.exists())
        assertEquals(File(rootDir, "resources"), fileSystem.rootDir())
    }
}
