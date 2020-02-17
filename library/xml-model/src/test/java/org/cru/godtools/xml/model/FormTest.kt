package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormTest {
    @Test
    fun testParseForm() {
        val form = Form(Manifest(), getXmlParserForResource("form.xml"))
        assertThat(form.content, contains(instanceOf(Image::class.java), instanceOf(Text::class.java)))
    }

    @Test
    fun testParseParagraphIgnoredContent() {
        val form = Form(Manifest(), getXmlParserForResource("form_ignored_content.xml"))
        assertThat(form.content, contains(instanceOf(Text::class.java)))
    }
}
