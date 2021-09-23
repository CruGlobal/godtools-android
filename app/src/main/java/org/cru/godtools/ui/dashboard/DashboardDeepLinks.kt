package org.cru.godtools.ui.dashboard

import android.net.Uri
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM

internal fun Uri.isDashboardLessonsDeepLink() =
    (scheme == "http" || scheme == "https") && host.equals(HOST_GODTOOLSAPP_COM, true) && path == "/lessons"
