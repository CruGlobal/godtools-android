package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import java.util.Locale
import org.cru.godtools.analytics.firebase.PARAM_LANGUAGE_SECONDARY
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_TOGGLE_LANGUAGE

class ToggleLanguageAnalyticsActionEvent(tool: String?, private val selectedLocale: Locale) :
    AnalyticsActionEvent(action = ACTION_TOGGLE_LANGUAGE) {
    override fun isForSystem(system: AnalyticsSystem) =
        system === AnalyticsSystem.FIREBASE || system === AnalyticsSystem.FACEBOOK

    override val appSection = tool

    override val firebaseParams get() = Bundle().apply {
        putString(PARAM_LANGUAGE_SECONDARY, selectedLocale.toLanguageTag())
    }
}
