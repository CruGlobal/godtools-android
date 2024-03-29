package org.cru.godtools.base

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileSystemTest {
    private val rootDir = File.createTempFile("abc", null).parentFile!!
    private val context = mockk<Context> { every { filesDir } returns rootDir }

    private lateinit var fileSystem: FileSystem

    @BeforeTest
    fun setup() {
        fileSystem = FileSystem(context, "resources")
    }

    @Test
    fun testExists() = runTest {
        assertTrue(fileSystem.exists())
        assertEquals(File(rootDir, "resources"), fileSystem.rootDir())
    }
}
