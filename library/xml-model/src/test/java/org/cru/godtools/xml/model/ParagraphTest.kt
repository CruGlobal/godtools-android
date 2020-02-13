package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParagraphTest {
    @Test
    fun testParseParagraph() {
        val paragraph = Paragraph.fromXml(Manifest(), getXmlParserForResource("paragraph.xml"))
        assertThat(paragraph.content, contains(instanceOf(Image::class.java)))
    }
}
