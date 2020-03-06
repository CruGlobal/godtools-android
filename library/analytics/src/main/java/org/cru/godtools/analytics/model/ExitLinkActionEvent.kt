package org.cru.godtools.analytics.model

import android.net.Uri

private const val ACTION_EXIT_LINK = "Exit Link Engaged"
private const val ADOBE_EXIT_LINK = "cru.mobileexitlink"

class ExitLinkActionEvent(link: Uri) : AnalyticsActionEvent(action = ACTION_EXIT_LINK) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE
    override val adobeAttributes = mapOf<String?, Any>(ADOBE_EXIT_LINK to link.toString())
}
