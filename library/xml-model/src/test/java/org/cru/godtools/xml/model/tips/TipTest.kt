package org.cru.godtools.xml.model.tips

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipTest {
    @Test
    fun verifyParse() {
        val tip = Tip(Manifest(TOOL_CODE), "name", getXmlParserForResource("tip.xml"))
        assertEquals("name", tip.id)
        assertEquals(Tip.Type.ASK, tip.type)
        assertEquals(2, tip.pages.size)
        assertEquals(0, tip.pages[0].position)
        assertEquals("Page 1", (tip.pages[0].content[0] as Text).text)
        assertEquals(1, tip.pages[1].position)
        assertEquals("Page 2", (tip.pages[1].content[0] as Text).text)
    }
}
