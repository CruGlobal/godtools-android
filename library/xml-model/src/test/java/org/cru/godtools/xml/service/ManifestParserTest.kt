package org.cru.godtools.xml.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.cru.godtools.base.FileManager
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.util.getInputStreamForResource
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val MANIFEST = "manifest.xml"

@RunWith(AndroidJUnit4::class)
class ManifestParserTest {
    private lateinit var fileManager: FileManager

    private lateinit var parser: ManifestParser

    @Before
    fun setup() {
        fileManager = mock()

        parser = ManifestParser(fileManager)
    }

    @Test
    fun verifyParseManifest() {
        fileManager.stub {
            onBlocking { getInputStream(MANIFEST) } doAnswer {
                this@ManifestParserTest.getInputStreamForResource("../model/manifest_empty.xml")
            }
        }
        val result = runBlocking { parser.parse(MANIFEST, TOOL_CODE, Locale.ENGLISH) }
        assertTrue(result is Result.Data)
        assertNotNull((result as Result.Data).manifest)
    }

    @Test
    fun verifyParseManifestMissing() {
        fileManager.stub {
            onBlocking { getInputStream(MANIFEST) } doAnswer { throw FileNotFoundException() }
        }
        val result = runBlocking { parser.parse(MANIFEST, TOOL_CODE, Locale.ENGLISH) }
        assertTrue(result is Result.Error.NotFound)
    }

    @Test
    fun verifyParseManifestReadError() {
        fileManager.stub {
            onBlocking { getInputStream(MANIFEST) } doAnswer { throw IOException() }
        }
        val result = runBlocking { parser.parse(MANIFEST, TOOL_CODE, Locale.ENGLISH) }
        assertTrue(result is Result.Error)
    }

    @Test
    fun verifyParseManifestInvalid() {
        fileManager.stub {
            onBlocking { getInputStream(MANIFEST) } doAnswer {
                this@ManifestParserTest.getInputStreamForResource("../model/image.xml")
            }
        }
        val result = runBlocking { parser.parse(MANIFEST, TOOL_CODE, Locale.ENGLISH) }
        assertTrue(result is Result.Error.Corrupted)
    }
}
