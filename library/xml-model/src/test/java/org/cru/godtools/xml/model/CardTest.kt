package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.model.tips.InlineTip
import org.cru.godtools.xml.model.tips.Tip
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardTest {
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
        Page(Manifest(TOOL_CODE), 0, null, getXmlParserForResource(file)).let { it.cards[0] }

    @Test
    fun verifyCardTips() {
        val tip1 = Tip(id = "tip1")
        val tip2 = Tip(id = "tip2")
        val page = Page(Manifest(tips = { listOf(tip1, tip2) }))
        val card = Card(page, 0, content = {
            listOf(
                InlineTip(it, "tip1"),
                Paragraph(it, content = { listOf(InlineTip(it, "tip2")) }),
                InlineTip(it, "tip3"),
                InlineTip(it, "tip1")
            )
        })

        assertEquals(3, card.tips.size)
        assertThat(card.tips, contains(tip1, tip2, tip1))
    }

    @Test
    fun verifyCardIsLastVisibleCard() {
        val page = Page(
            Manifest(),
            cards = { listOf(Card(it, isHidden = false), Card(it, isHidden = false), Card(it, isHidden = true)) }
        )

        assertFalse(page.cards[0].isLastVisibleCard)
        assertTrue(page.cards[1].isLastVisibleCard)
        assertFalse(page.cards[2].isLastVisibleCard)
    }
}
