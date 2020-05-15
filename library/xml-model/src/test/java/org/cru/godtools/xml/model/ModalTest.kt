package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModalTest {
    @Test
    fun testParseModal() {
        val listenerEvents = Event.Id.parse(TOOL_CODE, "listener1 listener2")
        val dismissListenerEvents = Event.Id.parse(TOOL_CODE, "dismiss-listener1 dismiss-listener2")
        val modal = Modal.fromXml(Manifest(TOOL_CODE), getXmlParserForResource("modal.xml"), 0)
        assertThat(modal.listeners, containsInAnyOrder(*listenerEvents.toTypedArray()))
        assertThat(modal.dismissListeners, containsInAnyOrder(*dismissListenerEvents.toTypedArray()))
        assertThat(modal.content, contains(instanceOf(Paragraph::class.java), instanceOf(Paragraph::class.java)))
        assertEquals("Thank you", modal.title!!.text)
    }
}
