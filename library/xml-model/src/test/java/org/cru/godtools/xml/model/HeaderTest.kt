package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderTest {
    @Test
    fun testParseHeader() {
        val page = Page(Manifest(TOOL_CODE), 0, null, getXmlParserForResource("header.xml"))
        val header = page.header!!
        assertEquals("5", header.number!!.text)
        assertEquals("title", header.title!!.text)
        assertEquals(Color.RED, header.backgroundColor)
        assertEquals("header-tip", header.tipId)
    }
}
