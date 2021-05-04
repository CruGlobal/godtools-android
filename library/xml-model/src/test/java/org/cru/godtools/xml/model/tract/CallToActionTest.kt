package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.EventId
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.model.mockManifest
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
        val events = EventId.parse(TOOL_CODE, "event1 ns:event2")
        val page = TractPage(mockManifest(), 0, null, getXmlParserForResource("call_to_action.xml"))
        val callToAction = page.callToAction

        assertEquals(Color.RED, callToAction.controlColor)
        assertThat(callToAction.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals("Call To Action", callToAction.label!!.text)
        assertEquals("tip1", callToAction.tipId)
    }
}
