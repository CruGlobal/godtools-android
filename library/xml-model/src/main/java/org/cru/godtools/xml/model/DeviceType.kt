package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import java.util.Collections
import java.util.EnumSet

enum class DeviceType {
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

        internal fun parse(types: String?, defValue: Set<DeviceType>) = when {
            types != null -> REGEX_SEQUENCE_SEPARATOR.split(types).mapNotNullTo(mutableSetOf()) { parseSingle(it) }
            else -> defValue
        }
    }
}
