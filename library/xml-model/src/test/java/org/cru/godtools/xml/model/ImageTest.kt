package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val TOOL_CODE = "test"

@RunWith(AndroidJUnit4::class)
class ImageTest {
    private val manifest = Manifest(TOOL_CODE)

    @Test
    fun testParseImage() {
        val events = Event.Id.parse(TOOL_CODE, "ns:event1 event2")
        val image = Image.fromXml(manifest, getXmlParserForResource("image.xml"))
        assertEquals("image.png", image.mResourceName)
        assertThat(image.events, containsInAnyOrder(*events.toTypedArray()))
    }

    @Test
    fun testParseImageRestricted() {
        val image = Image.fromXml(manifest, getXmlParserForResource("image_restricted.xml"))
        assertTrue(image.isIgnored)
    }
}
