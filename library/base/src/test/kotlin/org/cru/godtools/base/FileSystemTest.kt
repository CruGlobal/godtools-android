package org.cru.godtools.base

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FileSystemTest {
    private val rootDir = File.createTempFile("abc", null).parentFile!!
    private val context: Context = mockk { every { filesDir } returns rootDir }

    private val fileSystem = FileSystem(context, "resources")

    @Test
    fun testExists() = runTest {
        assertTrue(fileSystem.exists())
        assertEquals(File(rootDir, "resources"), fileSystem.rootDir())
    }
}
