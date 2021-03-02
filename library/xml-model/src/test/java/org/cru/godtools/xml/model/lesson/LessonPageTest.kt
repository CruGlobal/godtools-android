package org.cru.godtools.xml.model.lesson

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
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
        manifest = Manifest(TOOL_CODE, type = Manifest.Type.LESSON)
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
        assertEquals("background.png", page._backgroundImage)
        assertEquals(Color.RED, page.backgroundColor)
        assertEquals(ImageGravity.TOP or ImageGravity.END, page.backgroundImageGravity)
        assertEquals(ImageScaleType.FIT, page.backgroundImageScaleType)
        assertEquals(Event.Id.parse(TOOL_CODE, "lesson_page_event1"), page.listeners)
    }

    private fun parsePageXml(file: String, manifest: Manifest = this.manifest) =
        LessonPage(manifest, 0, null, getXmlParserForResource(file))
}
