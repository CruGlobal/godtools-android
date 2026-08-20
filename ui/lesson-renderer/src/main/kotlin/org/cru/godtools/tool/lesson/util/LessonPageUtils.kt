package org.cru.godtools.tool.lesson.util

import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage

/**
 * Convert the position of a page within the full list of manifest pages (which includes hidden pages) to the
 * index of the corresponding page within the initially visible lesson pages. Hidden pages resolve to the closest
 * previous visible page, and invalid positions resolve to the first page.
 */
internal fun Manifest.lessonPagerIndexForPagePosition(position: Int): Int {
    val page = pages.getOrNull(position)
        ?.let { page -> generateSequence(page) { it.previousPage }.firstOrNull { !it.isHidden } }
    return pages.filterIsInstance<LessonPage>().filterNot { it.isHidden }
        .indexOfFirst { it == page }
        .coerceAtLeast(0)
}
