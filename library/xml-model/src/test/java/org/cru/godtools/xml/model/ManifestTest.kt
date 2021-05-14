package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.xml.model.lesson.DEFAULT_LESSON_CONTROL_COLOR
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

class ManifestTest {
    @Test
    fun testManifestTypeParsing() {
        assertNull(Manifest.Type.parseOrNull(null))
        assertEquals(Manifest.Type.ARTICLE, Manifest.Type.parseOrNull("article"))
        assertEquals(Manifest.Type.LESSON, Manifest.Type.parseOrNull("lesson"))
        assertEquals(Manifest.Type.TRACT, Manifest.Type.parseOrNull("tract"))
        assertEquals(Manifest.Type.UNKNOWN, Manifest.Type.parseOrNull("nasldkja"))
    }
}

@RunWith(AndroidJUnit4::class)
class ManifestParsingRobolectricTest {
    @Test
    fun `parseManifest - Empty Manifest`() {
        val manifest = parseManifest("manifest_empty.xml")
        assertNull(manifest.title)
        assertEquals(DEFAULT_LESSON_CONTROL_COLOR, manifest.lessonControlColor)
        assertEquals(DEFAULT_TEXT_SCALE, manifest.textScale, 0.0001)
        assertEquals(0, manifest.aemImports.size)
        assertThat(manifest.lessonPages, `is`(empty()))
        assertThat(manifest.tractPages, `is`(empty()))
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    @Test
    fun `parseManifest - Lesson Manifest`() {
        val manifest = parseManifest("manifest_lesson.xml")
        assertEquals("title", manifest.title)
        assertEquals(Manifest.Type.LESSON, manifest.type)
        assertEquals(Color.RED, manifest.lessonControlColor)
        assertEquals(EventId.parse("dismiss_event").toSet(), manifest.dismissListeners)
        assertThat(manifest.tractPages, `is`(empty()))
        assertEquals(1, manifest.lessonPages.size)
        assertEquals("page0.xml", manifest.lessonPages[0].fileName)
    }

    @Test
    fun `parseManifest - Tract Manifest`() {
        val manifest = parseManifest("manifest_tract.xml")
        assertEquals("title", manifest.title)
        assertEquals(1.2345, manifest.textScale, 0.00001)
        assertEquals(Manifest.Type.TRACT, manifest.type)
        assertThat(manifest.lessonPages, `is`(empty()))
        assertEquals(2, manifest.tractPages.size)
        assertEquals("page0.xml", manifest.tractPages[0].fileName)
        assertEquals(0, manifest.tractPages[0].position)
        assertEquals(null, manifest.tractPages[1].fileName)
        assertEquals(1, manifest.tractPages[1].position)
    }

    @Test
    fun `parseManifest - Manifest Containing Tips`() {
        val manifest = parseManifest("manifest_tips.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(1, manifest.tips.size)
        assertEquals("tip1", manifest.findTip("tip1")!!.id)
    }

    @Test
    fun `parseManifest - Manifest With Invalid Tips`() {
        val manifest = parseManifest("manifest_tips_invalid.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    @Test
    fun testCardBackgroundColorFallbackBehavior() {
        assertEquals(Color.GREEN, Manifest(cardBackgroundColor = Color.GREEN).cardBackgroundColor)
        assertEquals(Color.BLUE, Manifest(backgroundColor = Color.BLUE).cardBackgroundColor)
    }

    // region navbar colors
    @Test
    fun testNavBarColors() {
        val manifestNull: Manifest? = null
        assertEquals(Manifest.DEFAULT_PRIMARY_COLOR, manifestNull.navBarColor)
        assertEquals(Manifest.DEFAULT_PRIMARY_TEXT_COLOR, manifestNull.navBarControlColor)

        val manifestPrimary = Manifest(primaryColor = Color.GREEN, primaryTextColor = Color.BLUE)
        assertEquals(Color.GREEN, manifestPrimary.navBarColor)
        assertEquals(Color.GREEN, (manifestPrimary as Manifest?).navBarColor)
        assertEquals(Color.BLUE, manifestPrimary.navBarControlColor)
        assertEquals(Color.BLUE, (manifestPrimary as Manifest?).navBarControlColor)

        val manifestNavBar = Manifest(
            primaryColor = Color.RED,
            primaryTextColor = Color.RED,
            navBarColor = Color.GREEN,
            navBarControlColor = Color.BLUE
        )
        assertEquals(Color.GREEN, manifestNavBar.navBarColor)
        assertEquals(Color.GREEN, (manifestNavBar as Manifest?).navBarColor)
        assertEquals(Color.BLUE, manifestNavBar.navBarControlColor)
        assertEquals(Color.BLUE, (manifestNavBar as Manifest?).navBarControlColor)
    }

    @Test
    fun testLessonNavBarColors() {
        val manifestNull: Manifest? = null
        assertEquals(Manifest.DEFAULT_LESSON_NAV_BAR_COLOR, manifestNull.lessonNavBarColor)
        assertEquals(Manifest.DEFAULT_PRIMARY_COLOR, manifestNull.lessonNavBarControlColor)

        val manifestPrimary =
            Manifest(type = Manifest.Type.LESSON, primaryColor = Color.GREEN, primaryTextColor = Color.RED)
        assertEquals(Manifest.DEFAULT_LESSON_NAV_BAR_COLOR, manifestPrimary.navBarColor)
        assertEquals(Manifest.DEFAULT_LESSON_NAV_BAR_COLOR, manifestPrimary.lessonNavBarColor)
        assertEquals(Color.GREEN, manifestPrimary.navBarControlColor)
        assertEquals(Color.GREEN, manifestPrimary.lessonNavBarControlColor)

        val manifestNavBar = Manifest(
            type = Manifest.Type.LESSON,
            primaryColor = Color.RED,
            primaryTextColor = Color.RED,
            navBarColor = Color.GREEN,
            navBarControlColor = Color.BLUE
        )
        assertEquals(Color.GREEN, manifestNavBar.navBarColor)
        assertEquals(Color.GREEN, manifestNavBar.lessonNavBarColor)
        assertEquals(Color.BLUE, manifestNavBar.navBarControlColor)
        assertEquals(Color.BLUE, manifestNavBar.lessonNavBarControlColor)
    }
    // endregion navbar colors

    private fun parseManifest(name: String) =
        Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource(name)) { getXmlParserForResource(it) }
}
