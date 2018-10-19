@file:JvmName("MediaTypeConverter")
package org.cru.godtools.articles.aem.room.converter

import android.arch.persistence.room.TypeConverter
import okhttp3.MediaType

@TypeConverter
fun toMediaType(raw: String?): MediaType? {
    return raw?.let { MediaType.parse(it) }
}

@TypeConverter
fun toString(type: MediaType?): String? {
    return type?.toString()
}
