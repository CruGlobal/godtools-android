package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import java.util.Collections
import java.util.EnumSet
import org.xmlpull.v1.XmlPullParser

internal enum class DeviceType {
    ANDROID, MOBILE, UNKNOWN;

    companion object {
        internal val ALL: Set<DeviceType> = Collections.unmodifiableSet(EnumSet.allOf(DeviceType::class.java))

        @VisibleForTesting
        internal const val XML_DEVICE_TYPE_ANDROID = "android"
        @VisibleForTesting
        internal const val XML_DEVICE_TYPE_MOBILE = "mobile"

        @VisibleForTesting
        internal fun parseSingle(type: String?) = when (type) {
            null -> null
            XML_DEVICE_TYPE_ANDROID -> ANDROID
            XML_DEVICE_TYPE_MOBILE -> MOBILE
            else -> UNKNOWN
        }

        internal fun parseOrNull(types: String?) = when (types) {
            null -> null
            else -> REGEX_SEQUENCE_SEPARATOR.split(types).mapNotNullTo(mutableSetOf()) { parseSingle(it) }
        }
    }
}

internal fun XmlPullParser.getAttributeValueAsDeviceTypesOrNull(name: String) =
    DeviceType.parseOrNull(getAttributeValue(null, name))
