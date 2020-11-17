package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentTest {
    @Test
    fun verifyFromXmlParagraph() {
        val content = Content.fromXml(Manifest(), getXmlParserForResource("paragraph.xml"), true)
        assertTrue(content is Paragraph)
    }

    @Test
    fun verifyFromXmlParagraphFallback() {
        val content = Content.fromXml(Manifest(), getXmlParserForResource("fallback_paragraph.xml"), true)
        assertTrue(content is Fallback)
    }

    @Test
    fun verifyFromXmlFallback() {
        val content = Content.fromXml(Manifest(), getXmlParserForResource("fallback.xml"), true)
        assertTrue(content is Fallback)
    }
}
