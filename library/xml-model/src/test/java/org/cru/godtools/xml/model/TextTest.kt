package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.tract.TractPage
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextTest {
    @Test
    fun testParseText() {
        val page = TractPage(mockManifest(), 0, null, getXmlParserForResource("text.xml"))
        val (text1, text2) = (page.hero!!.content[0] as Paragraph).content.filterIsInstance<Text>()

        assertEquals("Text 1", text1.text)
        assertEquals(1.0, text1.textScale, 0.001)
        assertEquals(Manifest.DEFAULT_TEXT_COLOR, text1.textColor)
        assertEquals(Text.Align.DEFAULT, text1.textAlign)

        assertEquals("Text 2", text2.text)
        assertEquals(1.23, text2.textScale, 0.001)
        assertEquals(Color.GREEN, text2.textColor)
        assertEquals(Text.Align.END, text2.textAlign)
    }
}
