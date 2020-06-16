package org.cru.godtools.xml.model

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
class CardTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun verifyParseCard() {
        val listenerEvents = Event.Id.parse(TOOL_CODE, "listener1 listener2")
        val dismissListenerEvents = Event.Id.parse(TOOL_CODE, "dismiss-listener1 dismiss-listener2")

        val card = parseCardXml("card.xml")
        assertEquals("$TOOL_CODE-0-0", card.id)
        assertEquals("Card 1", card.label!!.text)
        assertThat(card.listeners, containsInAnyOrder(*listenerEvents.toTypedArray()))
        assertThat(card.dismissListeners, containsInAnyOrder(*dismissListenerEvents.toTypedArray()))
        assertThat(card.content, contains(instanceOf(Paragraph::class.java)))
    }

    private fun parseCardXml(file: String) =
        Page(manifest, 0).apply { parsePageXml(getXmlParserForResource(file)) }.let { it.cards[0] }
}
