package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestTest {
    @Test
    fun verifyParseEmptyManifest() {
        val manifest = Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_empty.xml")) { TODO() }
        assertEquals(0, manifest.pages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    @Test
    fun verifyParseManifestPages() {
        val manifest = Manifest(
            TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_pages.xml")
        ) { getXmlParserForResource(it) }
        assertEquals(2, manifest.pages.size)
        assertEquals(0, manifest.pages[0].position)
        assertEquals(1, manifest.pages[1].position)
    }

    @Test
    fun verifyParseManifestWithTips() {
        val manifest = Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_tips.xml")) {
            getXmlParserForResource(it)
        }
        assertEquals(0, manifest.pages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(1, manifest.tips.size)
        assertEquals("tip1", manifest.findTip("tip1")!!.id)
    }

    @Test
    fun verifyParseManifestWithInvalidTips() {
        val manifest = Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_tips_invalid.xml")) {
            getXmlParserForResource(it)
        }
        assertEquals(0, manifest.pages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }
}
