package org.cru.godtools.tract.util

import android.content.Context
import android.net.Uri
import org.cru.godtools.tract.R

internal fun Uri.isTractDeepLink(context: Context) = ("http".equals(scheme, true) || "https".equals(scheme, true)) &&
    isTractDeepLinkHost(context) && pathSegments.size >= 2

private fun Uri.isTractDeepLinkHost(context: Context) =
    context.getString(R.string.tract_deeplink_host_1).equals(host, true) ||
        context.getString(R.string.tract_deeplink_host_2).equals(host, true)
