package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserException

@RunWith(AndroidJUnit4::class)
class FallbackTest {
    @Test
    fun testParseFallback() {
        val fallback = Fallback(Manifest(), getXmlParserForResource("fallback.xml"))
        assertThat(
            fallback.content,
            contains(instanceOf(Text::class.java), instanceOf(Text::class.java), instanceOf(Text::class.java))
        )
    }

    @Test
    fun testParseParagraphFallback() {
        val fallback = Fallback(Manifest(), getXmlParserForResource("fallback_paragraph.xml"))
        assertThat(
            fallback.content,
            contains(instanceOf(Text::class.java), instanceOf(Text::class.java), instanceOf(Text::class.java))
        )
    }

    @Test(expected = XmlPullParserException::class)
    fun testParseParagraphFallbackInvalid() {
        Fallback(Manifest(), getXmlParserForResource("paragraph.xml"))
    }
}
