package org.cru.godtools.tool.lesson

import android.net.Uri
import java.util.Locale
import org.cru.godtools.base.HOST_KNOWGOD_COM

internal data class LessonDeepLink(val lesson: String, val locale: Locale, val page: Int? = null) {
    companion object {
        // region https://knowgod.com/en/lesson/lessonhs/1
        private const val KNOWGOD_PATH_LOCALE = 0
        private const val KNOWGOD_PATH_LESSON = 2
        private const val KNOWGOD_PATH_PAGE = 3

        fun parseKnowGodDeepLink(uri: Uri): LessonDeepLink? {
            if (!isKnowGodDeepLink(uri)) return null

            return LessonDeepLink(
                lesson = uri.pathSegments[KNOWGOD_PATH_LESSON],
                locale = Locale.forLanguageTag(uri.pathSegments[KNOWGOD_PATH_LOCALE]),
                page = uri.pathSegments.getOrNull(KNOWGOD_PATH_PAGE)?.toIntOrNull()
            )
        }

        private fun isKnowGodDeepLink(uri: Uri) = (uri.scheme == "http" || uri.scheme == "https") &&
            uri.host.equals(HOST_KNOWGOD_COM, true) &&
            uri.pathSegments.size >= 3 &&
            uri.pathSegments[1].equals("lesson", true)
        // endregion https://knowgod.com/en/lesson/lessonhs/1
    }
}
