package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallToActionTest {
    @Test
    fun testParseCallToAction() {
        val events = Event.Id.parse(TOOL_CODE, "event1 ns:event2")
        val page = mockPage()
        page.parsePageXml(getXmlParserForResource("call_to_action.xml"))
        val callToAction = page.callToAction

        assertEquals(Color.RED, callToAction.controlColor)
        assertThat(callToAction.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals("Call To Action", callToAction.label!!.mText)
    }
}
