package org.cru.godtools.xml.model.lesson

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.xml.model.DEFAULT_TEXT_SCALE
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonPageTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE, type = Manifest.Type.LESSON, lessonControlColor = Color.RED)
    }

    @Test
    fun testParsePage() {
        val page = parsePageXml("page.xml")
        assertFalse(page.isHidden)
        assertEquals(Color.GREEN, page.controlColor)
        assertEquals(1.2345, page.textScale, 0.00001)
        assertEquals(1, page.content.size)
        assertTrue(page.content[0] is Text)
        assertEquals("background.png", page._backgroundImage)
        assertEquals(Color.RED, page.backgroundColor)
        assertEquals(ImageGravity.TOP or ImageGravity.END, page.backgroundImageGravity)
        assertEquals(ImageScaleType.FIT, page.backgroundImageScaleType)
        assertEquals(EventId.parse("lesson_page_event1").toSet(), page.listeners)
    }

    @Test
    fun testParsePageEmpty() {
        val page = parsePageXml("page_empty.xml")
        assertEquals(Color.RED, page.controlColor)
        assertEquals(DEFAULT_TEXT_SCALE, page.textScale, 0.001)
        assertThat(page.content, `is`(empty()))
    }

    @Test
    fun testParsePageHidden() {
        val page = parsePageXml("page_hidden.xml")
        assertTrue(page.isHidden)
    }

    @Test
    fun testTextScale() {
        assertEquals(DEFAULT_TEXT_SCALE, LessonPage(Manifest()).textScale, 0.001)
        assertEquals(2.0, LessonPage(Manifest(), textScale = 2.0).textScale, 0.001)

        val manifest = Manifest(textScale = 3.0)
        assertEquals(3.0, LessonPage(manifest).textScale, 0.001)
        assertEquals(6.0, LessonPage(manifest, textScale = 2.0).textScale, 0.001)
    }

    private fun parsePageXml(file: String, manifest: Manifest = this.manifest) =
        LessonPage(manifest, 0, null, getXmlParserForResource(file))
}
