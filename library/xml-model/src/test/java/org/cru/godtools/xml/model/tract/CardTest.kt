package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.EventId
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.TOOL_CODE
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
        val listenerEvents = EventId.parse(TOOL_CODE, "listener1 listener2")
        val dismissListenerEvents = EventId.parse(TOOL_CODE, "dismiss-listener1 dismiss-listener2")

        val card = parseCardXml("card.xml")
        assertEquals("$TOOL_CODE-0-0", card.id)
        assertEquals("Card 1", card.label!!.text)
        assertEquals(Color.RED, card.backgroundColor)
        assertThat(card.listeners, containsInAnyOrder(*listenerEvents.toTypedArray()))
        assertThat(card.dismissListeners, containsInAnyOrder(*dismissListenerEvents.toTypedArray()))
        assertThat(card.content, contains(instanceOf(Paragraph::class.java)))
    }

    private fun parseCardXml(file: String) =
        TractPage(Manifest(TOOL_CODE), 0, null, getXmlParserForResource(file)).let { it.cards[0] }

    @Test
    fun verifyCardTips() {
        val tip1 = Tip(id = "tip1")
        val tip2 = Tip(id = "tip2")
        val page = TractPage(Manifest(tips = { listOf(tip1, tip2) }))
        val card = Card(
            page,
            content = {
                listOf(
                    InlineTip(it, "tip1"),
                    Paragraph(it, content = { listOf(InlineTip(it, "tip2")) }),
                    InlineTip(it, "tip3"),
                    InlineTip(it, "tip1")
                )
            }
        )

        assertEquals(3, card.tips.size)
        assertThat(card.tips, contains(tip1, tip2, tip1))
    }

    @Test
    fun verifyCardIsLastVisibleCard() {
        val page = TractPage(
            Manifest(),
            cards = { listOf(Card(it, isHidden = false), Card(it, isHidden = false), Card(it, isHidden = true)) }
        )

        assertFalse(page.cards[0].isLastVisibleCard)
        assertTrue(page.cards[1].isLastVisibleCard)
        assertFalse(page.cards[2].isLastVisibleCard)
    }

    @Test
    fun testCardBackgroundColorFallbackBehavior() {
        val page = TractPage(Manifest(), cardBackgroundColor = Color.GREEN)
        assertEquals(Color.GREEN, Card(page).backgroundColor)
        assertEquals(Color.BLUE, Card(page, backgroundColor = Color.BLUE).backgroundColor)
    }
}
