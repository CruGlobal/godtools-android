package org.cru.godtools.tract.analytics.model

import java.util.Locale
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.cru.godtools.analytics.firebase.PARAM_LANGUAGE_SECONDARY
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_TOGGLE_LANGUAGE = "parallel_language_toggled"

class ToggleLanguageAnalyticsActionEvent(tool: String?, locale: Locale) :
    AnalyticsActionEvent(action = ACTION_TOGGLE_LANGUAGE) {
    override fun isForSystem(system: AnalyticsSystem) =
        system === AnalyticsSystem.FIREBASE || system === AnalyticsSystem.FACEBOOK

    override val appSection = tool
    @OptIn(ExperimentalStdlibApi::class)
    override val firebaseParams = buildMap<String, Any> {
        put(PARAM_LANGUAGE_SECONDARY, toLanguageTag(locale))
    }
}
