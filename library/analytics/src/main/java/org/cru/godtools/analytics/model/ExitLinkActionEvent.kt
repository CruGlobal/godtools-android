package org.cru.godtools.analytics.model

import android.net.Uri
import android.os.Bundle

private const val ACTION_EXIT_LINK = "exit_link_engaged"
private const val FIREBASE_PARAM_EXIT_LINK = "cru_mobileexitlink"

class ExitLinkActionEvent(private val tool: String?, private val link: Uri) : AnalyticsActionEvent(ACTION_EXIT_LINK) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE

    override val appSection get() = tool

    override val firebaseParams get() = Bundle().apply {
        putString(FIREBASE_PARAM_EXIT_LINK, link.toString())
    }
}
