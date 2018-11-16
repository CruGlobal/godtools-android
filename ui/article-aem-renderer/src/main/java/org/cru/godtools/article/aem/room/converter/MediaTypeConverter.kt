package org.cru.godtools.article.aem.room.converter

import androidx.room.TypeConverter
import okhttp3.MediaType

class MediaTypeConverter {
    @TypeConverter
    fun toMediaType(raw: String?): MediaType? {
        return raw?.let { MediaType.parse(it) }
    }

    @TypeConverter
    fun toString(type: MediaType?): String? {
        return type?.toString()
    }
}
