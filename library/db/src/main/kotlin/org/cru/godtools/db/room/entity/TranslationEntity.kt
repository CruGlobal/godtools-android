package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale
import org.cru.godtools.model.Translation

@Entity(
    tableName = "translations",
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["code"],
            childColumns = ["tool"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["locale"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("tool", "locale"),
        Index(
            "tool",
            "locale",
            "version",
            orders = [Index.Order.ASC, Index.Order.ASC, Index.Order.DESC]
        )
    ]
)
internal class TranslationEntity(
    @PrimaryKey
    val id: Long,
    val tool: String,
    val locale: Locale,
    val version: Int = Translation.DEFAULT_VERSION,
    val name: String?,
    val description: String?,
    val tagline: String?,
    val toolDetailsConversationStarters: String?,
    val toolDetailsOutline: String?,
    val toolDetailsBibleReferences: String?,
    val manifestFileName: String?,
    @ColumnInfo(defaultValue = "false")
    val isDownloaded: Boolean,
) {
    constructor(translation: Translation) : this(
        id = translation.id,
        tool = translation.toolCode.orEmpty(),
        locale = translation.languageCode,
        version = translation.version,
        name = translation.name,
        description = translation.description,
        tagline = translation.tagline,
        toolDetailsConversationStarters = translation.toolDetailsConversationStarters,
        toolDetailsOutline = translation.toolDetailsOutline,
        toolDetailsBibleReferences = translation.toolDetailsBibleReferences,
        manifestFileName = translation.manifestFileName,
        isDownloaded = translation.isDownloaded,
    )

    fun toModel() = Translation().also {
        it.id = id
        it.toolCode = tool
        it.languageCode = locale
        it.version = version
        it.name = name
        it.description = description
        it.tagline = tagline
        it.toolDetailsConversationStarters = toolDetailsConversationStarters
        it.toolDetailsOutline = toolDetailsOutline
        it.toolDetailsBibleReferences = toolDetailsBibleReferences
        it.manifestFileName = manifestFileName
        it.isDownloaded = isDownloaded
    }
}
