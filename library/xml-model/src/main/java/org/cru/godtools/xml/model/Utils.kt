@file:JvmName("Utils")

package org.cru.godtools.xml.model

import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import org.xmlpull.v1.XmlPullParser

private val REGEX_COLOR =
    "^\\s*rgba\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)\\s*$".toRegex()

@Deprecated("Use toBoolean extension method instead", ReplaceWith("raw?.toBoolean() ?: defaultValue"))
fun parseBoolean(raw: String?, defaultValue: Boolean) = raw?.toBoolean() ?: defaultValue

@ColorInt
@Deprecated(
    "Use getAttributeValueAsColorOrNull extension method instead",
    ReplaceWith("getAttributeValueAsColorOrNull(name) ?: defValue")
)
fun XmlPullParser.parseColor(name: String, @ColorInt defValue: Int?) = getAttributeValueAsColorOrNull(name) ?: defValue

@ColorInt
internal fun XmlPullParser.getAttributeValueAsColorOrNull(name: String) = getAttributeValueAsColorOrNull(null, name)

@ColorInt
internal fun XmlPullParser.getAttributeValueAsColorOrNull(namespace: String?, name: String) =
    getAttributeValue(namespace, name)?.parseColorOrNull()

@ColorInt
internal fun String.parseColorOrNull() = REGEX_COLOR.matchEntire(this)?.let {
    try {
        val (red, green, blue, alpha) = it.destructured
        Color.argb((alpha.toDouble() * 255).toInt(), red.toInt(), green.toInt(), blue.toInt())
    } catch (ignored: Exception) {
        null
    }
}

@Deprecated(
    "Use getAttributeValueAsImageScaleTypeOrNull extension method instead",
    ReplaceWith("getAttributeValueAsImageScaleTypeOrNull(attribute) ?: defValue")
)
fun XmlPullParser.parseScaleType(attribute: String, defValue: ImageScaleType?) =
    getAttributeValueAsImageScaleTypeOrNull(attribute) ?: defValue

@Deprecated(
    "Use getAttributeValueAsUriOrNull instead",
    ReplaceWith("getAttributeValueAsUriOrNull(attribute) ?: defValue")
)
fun XmlPullParser.parseUrl(attribute: String, defValue: Uri?) = getAttributeValueAsUriOrNull(attribute) ?: defValue

internal fun XmlPullParser.getAttributeValueAsUriOrNull(name: String) = getAttributeValue(null, name)?.toAbsoluteUri()
internal fun String.toAbsoluteUri(defaultScheme: String = "http"): Uri =
    Uri.parse(this).takeIf { it.isAbsolute } ?: Uri.parse("$defaultScheme://$this")
