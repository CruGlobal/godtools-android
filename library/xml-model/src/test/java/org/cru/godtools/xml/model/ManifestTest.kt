package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestTest {
    @Test
    fun testParseEmptyManifest() {
        val manifest = parseManifest("manifest_empty.xml")
        assertNull(manifest.title)
        assertEquals(0, manifest.aemImports.size)
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    @Test
    fun testParseManifestTract() {
        val manifest = parseManifest("manifest_tract.xml")
        assertEquals("title", manifest.title)
        assertEquals(Manifest.Type.TRACT, manifest.type)
        assertEquals(2, manifest.tractPages.size)
        assertEquals("page0.xml", manifest.tractPages[0].fileName)
        assertEquals(0, manifest.tractPages[0].position)
        assertEquals(null, manifest.tractPages[1].fileName)
        assertEquals(1, manifest.tractPages[1].position)
    }

    @Test
    fun testParseManifestWithTips() {
        val manifest = parseManifest("manifest_tips.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(1, manifest.tips.size)
        assertEquals("tip1", manifest.findTip("tip1")!!.id)
    }

    @Test
    fun testParseManifestWithInvalidTips() {
        val manifest = parseManifest("manifest_tips_invalid.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    private fun parseManifest(name: String) =
        Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource(name)) { getXmlParserForResource(it) }
}
