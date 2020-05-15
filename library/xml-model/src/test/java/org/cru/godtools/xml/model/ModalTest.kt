package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.R
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
class ModalTest {
    private lateinit var page: Page

    @Before
    fun setupPage() {
        page = Page(Manifest(TOOL_CODE), 0)
    }

    @Test
    fun testModalDefaults() {
        val modal = Modal(page, 0)

        assertEquals("$TOOL_CODE-0-0", modal.id)
        assertEquals(Color.WHITE, modal.textColor)
        assertEquals(Color.TRANSPARENT, modal.primaryColor)
        assertEquals(Color.WHITE, modal.primaryTextColor)
        assertEquals(Color.WHITE, modal.buttonColor)

        assertEquals(R.dimen.text_size_modal, modal.textSize)
        assertEquals(Text.Align.CENTER, modal.textAlign)
    }

    @Test
    fun testParseModal() {
        val listenerEvents = Event.Id.parse(TOOL_CODE, "listener1 listener2")
        val dismissListenerEvents = Event.Id.parse(TOOL_CODE, "dismiss-listener1 dismiss-listener2")

        val modal = Modal(page, 0, getXmlParserForResource("modal.xml"))
        assertThat(modal.listeners, containsInAnyOrder(*listenerEvents.toTypedArray()))
        assertThat(modal.dismissListeners, containsInAnyOrder(*dismissListenerEvents.toTypedArray()))
        assertThat(modal.content, contains(instanceOf(Paragraph::class.java), instanceOf(Paragraph::class.java)))
        assertEquals("Thank you", modal.title!!.text)
    }
}
