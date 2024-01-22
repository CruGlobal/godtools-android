package org.cru.godtools.db.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cru.godtools.model.DownloadedTranslationFile

@Entity(tableName = "downloadedTranslationFiles")
internal data class DownloadedTranslationFileEntity(@PrimaryKey @Embedded val key: Key) {
    companion object {
        fun DownloadedTranslationFile.toEntity() = DownloadedTranslationFileEntity(Key(translationId, filename))
    }

    internal data class Key(val translationId: Long, val filename: String)

    fun toModel() = DownloadedTranslationFile(key.translationId, key.filename)
}
