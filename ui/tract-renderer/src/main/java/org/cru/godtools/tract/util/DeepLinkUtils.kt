package org.cru.godtools.tract.util

import android.net.Uri
import org.cru.godtools.base.HOST_KNOWGOD_COM

internal fun Uri.isTractDeepLink() = ("http".equals(scheme, true) || "https".equals(scheme, true)) &&
    host.equals(HOST_KNOWGOD_COM, true) && pathSegments.size >= 2
