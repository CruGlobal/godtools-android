package org.cru.godtools.tract.util

import android.net.Uri
import org.cru.godtools.base.HOST_KNOWGOD_COM

internal fun Uri.isTractDeepLink() =
    (scheme == "http" || scheme == "https") && host.equals(HOST_KNOWGOD_COM, true) && pathSegments.size >= 2
