package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser

enum class ImageScaleType {
    FIT, FILL, FILL_X, FILL_Y;

    companion object {
        internal fun parseOrNull(value: String?) = when (value) {
            "fit" -> FIT
            "fill" -> FILL
            "fill-y" -> FILL_Y
            "fill-x" -> FILL_X
            else -> null
        }
    }
}

internal fun XmlPullParser.getAttributeValueAsImageScaleTypeOrNull(name: String) =
    ImageScaleType.parseOrNull(getAttributeValue(null, name))
