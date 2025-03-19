package org.cru.godtools.tool.tract

import android.net.Uri
import java.util.Locale
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.HOST_KNOWGOD_COM

internal data class TractDeepLink(
    val tool: String,
    val primaryLocales: List<Locale>,
    val parallelLocales: List<Locale>,
    val activeLocale: Locale,
    val page: Int? = null
) {
    companion object {
        private const val PARAM_USE_DEVICE_LANGUAGE = "useDeviceLanguage"
        private const val PARAM_PRIMARY_LANGUAGE = "primaryLanguage"
        private const val PARAM_PARALLEL_LANGUAGE = "parallelLanguage"

        // region https://knowgod.com/en/tool/v1/kgp/1
        private const val KNOWGOD_PATH_LOCALE = 0
        private const val KNOWGOD_PATH_TOOL = 3
        private const val KNOWGOD_PATH_PAGE = 4

        fun parseKnowGodDeepLink(uri: Uri): TractDeepLink? {
            if (!isKnowGodDeepLink(uri)) return null

            val primary = LinkedHashSet<Locale>()
            val parallel = LinkedHashSet<Locale>()
            val active = Locale.forLanguageTag(uri.pathSegments[KNOWGOD_PATH_LOCALE])

            if (!uri.getQueryParameter(PARAM_USE_DEVICE_LANGUAGE).isNullOrEmpty()) {
                primary += sequenceOf(Locale.getDefault()).includeFallbacks()
            }
            primary += uri.extractLanguagesFromDeepLinkParam(PARAM_PRIMARY_LANGUAGE).includeFallbacks()
            parallel += uri.extractLanguagesFromDeepLinkParam(PARAM_PARALLEL_LANGUAGE).includeFallbacks()

            if (active !in primary && active !in parallel) primary += sequenceOf(active).includeFallbacks()

            return TractDeepLink(
                tool = uri.pathSegments[KNOWGOD_PATH_TOOL],
                primaryLocales = primary.toList(),
                parallelLocales = parallel.toList(),
                activeLocale = active,
                page = uri.pathSegments.getOrNull(KNOWGOD_PATH_PAGE)?.toIntOrNull()
            )
        }

        private fun isKnowGodDeepLink(uri: Uri) = (uri.scheme == "http" || uri.scheme == "https") &&
            uri.host.equals(HOST_KNOWGOD_COM, true) &&
            uri.pathSegments.size >= 4 &&
            uri.pathSegments[1].equals("tool", true) &&
            uri.pathSegments[2].equals("v1", true)
        // endregion https://knowgod.com/en/tool/v1/kgp/1

        private fun Uri.extractLanguagesFromDeepLinkParam(param: String) = getQueryParameters(param)
            .asSequence()
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .map { Locale.forLanguageTag(it) }
    }
}
