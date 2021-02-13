package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextTest {
    @Test
    fun testParseTextDefaults() {
        val manifest = spy(Manifest())
        val text = Text(manifest, getXmlParserForResource("text_defaults.xml"))

        assertEquals("Text Defaults", text.text)
        assertEquals(DEFAULT_TEXT_SCALE, text.textScale, 0.001)
        assertEquals(Manifest.DEFAULT_TEXT_COLOR, text.textColor)
        assertEquals(Text.Align.DEFAULT, text.textAlign)
        assertEquals(DEFAULT_IMAGE_SIZE, text.startImageSize)
        assertEquals(DEFAULT_IMAGE_SIZE, text.endImageSize)
    }

    @Test
    fun testParseTextAttributes() {
        val parent = mock<Base>()
        val text = Text(parent, getXmlParserForResource("text_attributes.xml"))

        assertEquals("Attributes", text.text)
        assertEquals(1.23, text.textScale, 0.001)
        assertEquals(Color.GREEN, text.textColor)
        assertEquals(Text.Align.END, text.textAlign)
        assertEquals("start.png", text.startImageName)
        assertEquals(5, text.startImageSize)
        assertEquals("end.png", text.endImageName)
        assertEquals(11, text.endImageSize)
    }

    @Test
    fun testStartImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        assertEquals(resource, Text(manifest, "text", startImage = "image.png").startImage)
        assertNull(Text(manifest, "text", startImage = null).startImage)
    }

    @Test
    fun testEndImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        assertEquals(resource, Text(manifest, "text", endImage = "image.png").endImage)
        assertNull(Text(manifest, "text", endImage = null).endImage)
    }
}
