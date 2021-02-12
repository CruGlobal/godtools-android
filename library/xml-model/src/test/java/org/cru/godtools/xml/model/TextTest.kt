package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
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

    @Test
    fun testTextStartImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        val text = Text(manifest, "text", startImage = "image.png", startImageSize = 65)
        assertEquals(resource, text.startImage)
        assertEquals(65, text.startImageSize)
    }

    @Test
    fun testTextStartImageNull() {
        val text = Text(mockManifest(), "text", startImage = null)
        assertEquals(null, text.startImage)
        assertEquals(40, text.startImageSize)
    }

    @Test
    fun testTextEndImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        val text = Text(manifest, "text", endImage = "image.png", endImageSize = 65)
        assertEquals(resource, text.endImage)
        assertEquals(65, text.endImageSize)
    }

    @Test
    fun testTextEndImageNull() {
        val text = Text(mockManifest(), "text", endImage = null)
        assertEquals(null, text.endImage)
        assertEquals(40, text.endImageSize)
    }
}
