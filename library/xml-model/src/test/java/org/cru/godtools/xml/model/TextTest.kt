package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextTest {
    @Test
    fun testParseText() {
        val page = mockPage()
        page.parsePageXml(getXmlParserForResource("text.xml"))
        val (text1, text2) = (page.hero!!.content[0] as Paragraph).content.filterIsInstance<Text>()

        assertEquals("Text 1", text1.mText)
        assertEquals(1.0, Text.getTextScale(text1), 0.001)
        assertEquals(Manifest.getDefaultTextColor(), Text.getTextColor(text1))
        assertEquals(Text.Align.DEFAULT, Text.getTextAlign(text1))

        assertEquals("Text 2", text2.mText)
        assertEquals(1.23, Text.getTextScale(text2), 0.001)
        assertEquals(Color.GREEN, Text.getTextColor(text2))
        assertEquals(Text.Align.END, Text.getTextAlign(text2))
    }
}
