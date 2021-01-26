package org.cru.godtools.xml.model.lesson

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonPageTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(type = Manifest.Type.LESSON)
    }

    @Test
    fun testParseEmptyPage() {
        val page = parsePageXml("page_empty.xml")
        assertThat(page.content, `is`(empty()))
    }

    @Test
    fun testParsePage() {
        val page = parsePageXml("page.xml")
        assertEquals(1, page.content.size)
        assertTrue(page.content[0] is Text)
    }

    private fun parsePageXml(file: String, manifest: Manifest = this.manifest) =
        LessonPage(manifest, 0, null, getXmlParserForResource(file))
}
