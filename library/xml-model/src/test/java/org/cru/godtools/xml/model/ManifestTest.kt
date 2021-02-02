package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
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
    fun testParseEmptyManifest() {
        val manifest = parseManifest("manifest_empty.xml")
        assertNull(manifest.title)
        assertEquals(0, manifest.aemImports.size)
        assertThat(manifest.lessonPages, `is`(empty()))
        assertThat(manifest.tractPages, `is`(empty()))
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    @Test
    fun testParseManifestLesson() {
        val manifest = parseManifest("manifest_lesson.xml")
        assertEquals("title", manifest.title)
        assertEquals(Manifest.Type.LESSON, manifest.type)
        assertThat(manifest.tractPages, `is`(empty()))
        assertEquals(1, manifest.lessonPages.size)
        assertEquals("page0.xml", manifest.lessonPages[0].fileName)
    }

    @Test
    fun testParseManifestTract() {
        val manifest = parseManifest("manifest_tract.xml")
        assertEquals("title", manifest.title)
        assertEquals(Manifest.Type.TRACT, manifest.type)
        assertThat(manifest.lessonPages, `is`(empty()))
        assertEquals(2, manifest.tractPages.size)
        assertEquals("page0.xml", manifest.tractPages[0].fileName)
        assertEquals(0, manifest.tractPages[0].position)
        assertEquals(null, manifest.tractPages[1].fileName)
        assertEquals(1, manifest.tractPages[1].position)
    }

    @Test
    fun testParseManifestWithTips() {
        val manifest = parseManifest("manifest_tips.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(1, manifest.tips.size)
        assertEquals("tip1", manifest.findTip("tip1")!!.id)
    }

    @Test
    fun testParseManifestWithInvalidTips() {
        val manifest = parseManifest("manifest_tips_invalid.xml")
        assertEquals(0, manifest.tractPages.size)
        assertEquals(0, manifest.resources.size)
        assertEquals(0, manifest.tips.size)
    }

    // region navbar colors
    @Test
    fun testNavBarColors() {
        val nullManifest: Manifest? = null
        assertEquals(Manifest.DEFAULT_PRIMARY_COLOR, nullManifest.navBarColor)
        assertEquals(Manifest.DEFAULT_PRIMARY_TEXT_COLOR, nullManifest.navBarControlColor)

        val manifestPrimary = Manifest(primaryColor = Color.GREEN, primaryTextColor = Color.BLUE)
        assertEquals(Color.GREEN, (manifestPrimary as Manifest?).navBarColor)
        assertEquals(Color.BLUE, (manifestPrimary as Manifest?).navBarControlColor)

        val manifestNavBar = Manifest(
            primaryColor = Color.RED,
            primaryTextColor = Color.RED,
            navBarColor = Color.GREEN,
            navBarControlColor = Color.BLUE
        )
        assertEquals(Color.GREEN, (manifestNavBar as Manifest?).navBarColor)
        assertEquals(Color.BLUE, (manifestNavBar as Manifest?).navBarControlColor)
    }
    // endregion navbar colors

    private fun parseManifest(name: String) =
        Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource(name)) { getXmlParserForResource(it) }
}
