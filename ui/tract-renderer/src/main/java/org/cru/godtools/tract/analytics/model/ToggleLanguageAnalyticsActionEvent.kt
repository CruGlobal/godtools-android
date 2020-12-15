package org.cru.godtools.tract.analytics.model

import java.util.Locale
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.cru.godtools.analytics.adobe.ADOBE_ATTR_LANGUAGE_SECONDARY
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_TOGGLE_LANGUAGE = "Parallel Language Toggled"

private const val ADOBE_ATTR_TOGGLE_LANGUAGE = "cru.parallellanguagetoggle"

class ToggleLanguageAnalyticsActionEvent(tool: String?, locale: Locale) :
    AnalyticsActionEvent(action = ACTION_TOGGLE_LANGUAGE) {
    override fun isForSystem(system: AnalyticsSystem) =
        system === AnalyticsSystem.ADOBE || system === AnalyticsSystem.FACEBOOK

    override val appSection = tool
    @OptIn(ExperimentalStdlibApi::class)
    override val adobeAttributes = buildMap<String, Any> {
        put(ADOBE_ATTR_TOGGLE_LANGUAGE, 1)
        put(ADOBE_ATTR_LANGUAGE_SECONDARY, toLanguageTag(locale))
    }
    override val firebaseEventName = "parallel_language_toggled"
}
