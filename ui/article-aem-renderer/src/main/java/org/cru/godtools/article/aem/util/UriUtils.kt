package org.cru.godtools.article.aem.util

import android.net.Uri

fun Uri.addExtension(extension: String): Uri {
    return buildUpon().encodedPath((encodedPath ?: "") + ".$extension").build()
}
