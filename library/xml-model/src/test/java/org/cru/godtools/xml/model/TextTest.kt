package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextTest {
    // region Parsing
    @Test
    fun `testTextParsing - Defaults`() {
        val manifest = spy(Manifest())
        val text = Text(manifest, getXmlParserForResource("text_defaults.xml"))

        assertEquals("Text Defaults", text.text)
        assertEquals(DEFAULT_TEXT_SCALE, text.textScale, 0.001)
        assertEquals(Manifest.DEFAULT_TEXT_COLOR, text.textColor)
        assertEquals(Text.Align.DEFAULT, text.textAlign)
        assertEquals(Text.DEFAULT_IMAGE_SIZE, text.startImageSize)
        assertEquals(Text.DEFAULT_IMAGE_SIZE, text.endImageSize)
        assertThat(text.textStyles, empty())
    }

    @Test
    fun `testTextParsing - Attributes`() {
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
        assertThat(text.textStyles, containsInAnyOrder(Text.Style.BOLD, Text.Style.ITALIC))
    }
    // endregion Parsing

    @Test
    fun testStartImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        assertSame(resource, Text(manifest, startImage = "image.png").startImage)
        assertNull(Text(manifest, startImage = null).startImage)
    }

    @Test
    fun testEndImage() {
        val resource = mock<Resource>()
        val manifest = spy(Manifest()) { on { getResource("image.png") } doReturn resource }
        assertSame(resource, Text(manifest, endImage = "image.png").endImage)
        assertNull(Text(manifest, endImage = null).endImage)
    }
}
