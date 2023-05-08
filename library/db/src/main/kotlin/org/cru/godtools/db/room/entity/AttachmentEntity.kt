package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.cru.godtools.model.Attachment

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["code"],
            childColumns = ["tool"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal class AttachmentEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(index = true)
    val tool: String?,
    val filename: String?,
    val sha256: String?,
    @ColumnInfo(defaultValue = "false")
    var isDownloaded: Boolean = false,
) {
    constructor(attachment: Attachment) : this(
        id = attachment.id,
        tool = attachment.toolCode,
        filename = attachment.filename,
        sha256 = attachment.sha256,
        isDownloaded = attachment.isDownloaded
    )

    fun toModel() = Attachment().also {
        it.id = id
        it.toolCode = tool
        it.filename = filename
        it.sha256 = sha256
        it.isDownloaded = isDownloaded
    }
}
