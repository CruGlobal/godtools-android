package org.cru.godtools.ui.languages

import android.net.Uri
import org.cru.godtools.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.ui.circuit.CircuitDeepLinkParser

object LanguageSettingsDeepLinkParser : CircuitDeepLinkParser {
    override fun isDeepLinkSupported(uri: Uri) = when {
        uri.scheme in listOf("https", "http") &&
            uri.host == HOST_GODTOOLSAPP_COM &&
            uri.path == "/deeplink/settings/language" -> true

        uri.scheme == "godtools" &&
            uri.host == HOST_GODTOOLS_CUSTOM_URI &&
            uri.path == "/settings/language" -> true

        else -> false
    }

    // TODO: include the DashboardScreen as the parent screen once it's implemented
    override fun parseDeepLink(uri: Uri) = listOf(LanguageSettingsScreen)
}
