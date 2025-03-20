package org.cru.godtools.tool.cyoa

import android.net.Uri
import java.util.Locale
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.HOST_KNOWGOD_COM

internal data class CyoaDeepLink(
    val tool: String,
    val primaryLocales: List<Locale>,
    val parallelLocales: List<Locale>,
    val activeLocale: Locale,
    val page: String? = null,
) {
    companion object {
        // region https://knowgod.com/en/tool/v2/openers/page_id
        private const val KNOWGOD_PATH_LOCALE = 0
        private const val KNOWGOD_PATH_TOOL = 3
        private const val KNOWGOD_PATH_PAGE = 4

        fun parseKnowGodDeepLink(uri: Uri): CyoaDeepLink? {
            if (!isKnowGodDeepLink(uri)) return null

            val locale = Locale.forLanguageTag(uri.pathSegments[KNOWGOD_PATH_LOCALE])

            return CyoaDeepLink(
                tool = uri.pathSegments[KNOWGOD_PATH_TOOL],
                primaryLocales = sequenceOf(locale).includeFallbacks().toList(),
                parallelLocales = emptyList(),
                activeLocale = locale,
                page = uri.pathSegments.getOrNull(KNOWGOD_PATH_PAGE),
            )
        }

        private fun isKnowGodDeepLink(uri: Uri) = (uri.scheme == "http" || uri.scheme == "https") &&
            uri.host.equals(HOST_KNOWGOD_COM, true) &&
            uri.pathSegments.size >= 4 &&
            uri.pathSegments[1].equals("tool", true) &&
            uri.pathSegments[2].equals("v2", true)
        // endregion https://knowgod.com/en/tool/v2/openers/page_id
    }
}
