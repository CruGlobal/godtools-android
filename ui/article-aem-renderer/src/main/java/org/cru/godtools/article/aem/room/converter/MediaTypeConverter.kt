package org.cru.godtools.article.aem.room.converter

import androidx.room.TypeConverter
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull

object MediaTypeConverter {
    @JvmStatic
    @TypeConverter
    fun toMediaType(raw: String?) = raw?.toMediaTypeOrNull()

    @JvmStatic
    @TypeConverter
    fun toString(type: MediaType?) = type?.toString()
}
