package org.cru.godtools.tool.lesson.util

import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage

class LessonPageUtilsTest {
    // pages: 0 & 1 visible, 2-4 hidden, 5 visible
    private val manifest = Manifest(
        type = Manifest.Type.LESSON,
        pages = { manifest ->
            listOf(
                LessonPage(manifest, id = "page0"),
                LessonPage(manifest, id = "page1"),
                LessonPage(manifest, id = "page2", isHidden = true),
                LessonPage(manifest, id = "page3", isHidden = true),
                LessonPage(manifest, id = "page4", isHidden = true),
                LessonPage(manifest, id = "page5"),
            )
        },
    )

    @Test
    fun `lessonPagerIndexForPagePosition() - Visible Pages`() {
        assertEquals(0, manifest.lessonPagerIndexForPagePosition(0))
        assertEquals(1, manifest.lessonPagerIndexForPagePosition(1))
        assertEquals(2, manifest.lessonPagerIndexForPagePosition(5))
    }

    @Test
    fun `lessonPagerIndexForPagePosition() - Hidden Pages resolve to the closest previous visible page`() {
        assertEquals(1, manifest.lessonPagerIndexForPagePosition(2))
        assertEquals(1, manifest.lessonPagerIndexForPagePosition(3))
        assertEquals(1, manifest.lessonPagerIndexForPagePosition(4))
    }

    @Test
    fun `lessonPagerIndexForPagePosition() - Invalid positions resolve to the first page`() {
        assertEquals(0, manifest.lessonPagerIndexForPagePosition(6))
        assertEquals(0, manifest.lessonPagerIndexForPagePosition(-1))
    }

    @Test
    fun `lessonPagerIndexForPagePosition() - Hidden first page resolves to the first visible page`() {
        val manifest = Manifest(
            type = Manifest.Type.LESSON,
            pages = { manifest ->
                listOf(
                    LessonPage(manifest, id = "page0", isHidden = true),
                    LessonPage(manifest, id = "page1"),
                )
            },
        )

        assertEquals(0, manifest.lessonPagerIndexForPagePosition(0))
    }
}
