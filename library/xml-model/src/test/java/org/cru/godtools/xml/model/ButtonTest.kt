package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseButtonEvent() {
        val events = Event.Id.parse(TOOL_CODE, "ns:event1 event2")
        val button = Button.fromXml(manifest, getXmlParserForResource("button_event.xml"))
        assertThat(button.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals("event button", button.text!!.text)
        assertEquals(Color.RED, button.buttonColor)
    }

    @Test
    fun testParseButtonUrl() {
        val button = Button.fromXml(manifest, getXmlParserForResource("button_url.xml"))
        assertEquals(Button.Type.URL, button.type)
        assertEquals("https://www.google.com/", button.url!!.toString())
        assertEquals("url button", button.text!!.text)
        assertThat(button.analyticsEvents, contains(instanceOf(AnalyticsEvent::class.java)))
    }
}
