package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallToActionTest {
    private lateinit var manifest: Manifest
    private lateinit var page: Page

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
        page = Page(manifest, 0)
    }

    @Test
    fun testParseCallToAction() {
        val events = Event.Id.parse(TOOL_CODE, "event1 ns:event2")
        page.parsePageXml(getXmlParserForResource("call_to_action.xml"))
        val callToAction = page.callToAction
        assertEquals(Color.RED, callToAction.controlColor)
        assertThat(callToAction.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals("Call To Action", callToAction.label!!.mText)
    }
}
