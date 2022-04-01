package org.cru.godtools.ui.dashboard

import android.net.Uri
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.ui.dashboard.Page

internal fun Uri.isDashboardLessonsDeepLink() =
    (scheme == "http" || scheme == "https") && host.equals(HOST_GODTOOLSAPP_COM, true) && path == "/lessons"

internal fun findPageByUriPathSegment(segment: String?) = when (segment) {
    "lessons" -> Page.LESSONS
    "tools" -> Page.ALL_TOOLS
    "home" -> Page.FAVORITE_TOOLS
    else -> Page.FAVORITE_TOOLS
}
