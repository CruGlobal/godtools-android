package org.cru.godtools.article.aem.util

import android.net.Uri

private const val HOST_CRU_ORG = "cru.org"

private val REGEX_REMOVE_EXTENSION = "\\.[^/]*$".toRegex()

fun Uri.addExtension(extension: String): Uri = buildUpon().encodedPath((encodedPath ?: "") + ".$extension").build()

fun Uri.removeExtension(): Uri = encodedPath
    ?.takeIf { REGEX_REMOVE_EXTENSION.containsMatchIn(it) }
    ?.let { REGEX_REMOVE_EXTENSION.replace(it, "") }
    ?.let { buildUpon().encodedPath(it).build() } ?: this

internal fun Uri.isTrustedAemUri() =
    scheme == "https" && (host == HOST_CRU_ORG || host?.endsWith(".$HOST_CRU_ORG") == true)
