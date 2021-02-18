package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccordionTest {
    @Test
    fun testParseAccordion() {
        val accordion = Accordion(Manifest(), getXmlParserForResource("accordion.xml"))
        assertThat(accordion.sections, hasSize(2))

        with(accordion.sections[0]) {
            assertEquals("Section 1", header!!.text)
            assertThat(content, hasSize(1))
            assertThat(content[0], instanceOf(Text::class.java))
        }

        with(accordion.sections[1]) {
            assertEquals("Section 2", header!!.text)
            assertThat(content, hasSize(1))
            assertThat(content[0], instanceOf(Image::class.java))
        }
    }
}
