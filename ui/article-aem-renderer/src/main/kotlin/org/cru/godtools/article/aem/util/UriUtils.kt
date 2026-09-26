package org.cru.godtools.article.aem.util

import android.net.Uri
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val HOST_CRU_ORG = "cru.org"

private val REGEX_REMOVE_EXTENSION = "\\.[^/]*$".toRegex()

fun Uri.addExtension(extension: String): Uri = buildUpon().encodedPath((encodedPath ?: "") + ".$extension").build()

fun Uri.removeExtension(): Uri = encodedPath
    ?.takeIf { REGEX_REMOVE_EXTENSION.containsMatchIn(it) }
    ?.let { REGEX_REMOVE_EXTENSION.replace(it, "") }
    ?.let { buildUpon().encodedPath(it).build() } ?: this

// parse with OkHttp so the host we check is the host that will actually be requested
internal fun Uri.isTrustedAemUri() = toString().toHttpUrlOrNull()
    ?.takeIf { it.isHttps }
    ?.let { it.host == HOST_CRU_ORG || it.host.endsWith(".$HOST_CRU_ORG") } == true
