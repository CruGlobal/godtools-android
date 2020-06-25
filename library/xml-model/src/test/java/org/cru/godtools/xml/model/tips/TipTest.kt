package org.cru.godtools.xml.model.tips

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest =
            Manifest(TOOL_CODE)
    }

    @Test
    fun verifyParse() {
        val tip = Tip(manifest, "name", getXmlParserForResource("tip.xml"))
        assertEquals("name", tip.id)
        assertEquals(2, tip.pages.size)
        assertEquals("Page 1", (tip.pages[0].content[0] as Text).text)
        assertEquals("Page 2", (tip.pages[1].content[0] as Text).text)
    }
}
