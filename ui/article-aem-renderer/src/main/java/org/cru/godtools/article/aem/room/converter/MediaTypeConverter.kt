package org.cru.godtools.article.aem.room.converter

import androidx.room.TypeConverter
import okhttp3.MediaType

object MediaTypeConverter {
    @JvmStatic
    @TypeConverter
    fun toMediaType(raw: String?) = raw?.let { MediaType.parse(it) }

    @JvmStatic
    @TypeConverter
    fun toString(type: MediaType?) = type?.toString()
}
