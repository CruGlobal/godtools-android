package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.DEFAULT_TEXT_SCALE
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TractPageTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun verifyParse() {
        val page = parsePageXml("page.xml")
        assertEquals(Color.RED, page.backgroundColor)
        assertTrue(page.backgroundImageGravity.isTop)
        assertTrue(page.backgroundImageGravity.isStart)
        assertEquals(ImageScaleType.FILL, page.backgroundImageScaleType)
        assertEquals(1.2345, page.textScale, 0.00001)
        assertEquals("header", page.header!!.title!!.text)
        assertEquals("hero", page.hero!!.heading!!.text)
        assertEquals("call to action", page.callToAction.label!!.text)
        assertThat(page.cards, hasSize(0))
        assertThat(page.modals, hasSize(0))
    }

    @Test
    fun verifyParseCards() {
        val page = parsePageXml("page_cards.xml")
        assertThat(page.cards, hasSize(2))
        assertEquals("Card 1", page.cards[0].label!!.text)
        assertEquals("Card 2", page.cards[1].label!!.text)
    }

    @Test
    fun verifyParseModals() {
        val page = parsePageXml("page_modals.xml")
        assertThat(page.modals, hasSize(2))
        assertEquals("Modal 1", page.modals[0].title!!.text)
        assertEquals("Modal 2", page.modals[1].title!!.text)
    }

    @Test
    fun testCardBackgroundColorFallbackBehavior() {
        assertEquals(Color.GREEN, TractPage(Manifest(), cardBackgroundColor = Color.GREEN).cardBackgroundColor)
        assertEquals(Color.BLUE, TractPage(Manifest(cardBackgroundColor = Color.BLUE)).cardBackgroundColor)
    }

    @Test
    fun testTextScale() {
        assertEquals(DEFAULT_TEXT_SCALE, TractPage(Manifest()).textScale, 0.001)
        assertEquals(2.0, TractPage(Manifest(), textScale = 2.0).textScale, 0.001)

        val manifest = Manifest(textScale = 3.0)
        assertEquals(3.0, TractPage(manifest).textScale, 0.001)
        assertEquals(6.0, TractPage(manifest, textScale = 2.0).textScale, 0.001)
    }

    private fun parsePageXml(file: String) = TractPage(manifest, 0, null, getXmlParserForResource(file))
}
