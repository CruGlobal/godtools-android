package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cru.godtools.model.DownloadedFile

@Entity(tableName = "downloadedFiles")
internal data class DownloadedFileEntity(@PrimaryKey val filename: String) {
    companion object {
        fun DownloadedFile.toEntity() = DownloadedFileEntity(filename)
    }

    fun toModel() = DownloadedFile(filename)
}
