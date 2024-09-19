package org.cru.godtools.analytics.model

import android.net.Uri
import androidx.core.os.bundleOf
import java.util.Locale

private const val ACTION_EXIT_LINK = "exit_link_engaged"
private const val FIREBASE_PARAM_EXIT_LINK = "cru_mobileexitlink"

class ExitLinkActionEvent(private val tool: String?, private val link: String, locale: Locale? = null) :
    AnalyticsActionEvent(ACTION_EXIT_LINK, locale = locale) {
    constructor(tool: String?, link: Uri, locale: Locale? = null) : this(tool, link.toString(), locale)

    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE

    override val appSection get() = tool

    override val firebaseParams get() = bundleOf(FIREBASE_PARAM_EXIT_LINK to link)
}
