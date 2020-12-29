package org.cru.godtools.analytics.model

import android.net.Uri

private const val ACTION_EXIT_LINK = "exit_link_engaged"
private const val FIREBASE_PARAM_EXIT_LINK = "cru_mobileexitlink"

class ExitLinkActionEvent(link: Uri) : AnalyticsActionEvent(action = ACTION_EXIT_LINK) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
    override val adobeAttributes = mapOf<String, Any>(FIREBASE_PARAM_EXIT_LINK to link.toString())
}
