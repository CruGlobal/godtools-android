@file:JvmName("UriUtils")
package org.cru.godtools.article.aem.util

import android.net.Uri

private val REGEX_REMOVE_EXTENSION = "\\.[^/]*$".toRegex()

fun Uri.addExtension(extension: String): Uri {
    return buildUpon().encodedPath((encodedPath ?: "") + ".$extension").build()
}

fun Uri.removeExtension(): Uri {
    return encodedPath
        ?.takeIf { REGEX_REMOVE_EXTENSION.containsMatchIn(it) }
        ?.let { REGEX_REMOVE_EXTENSION.replace(it, "") }
        ?.let { buildUpon().encodedPath(it).build() } ?: this
}
