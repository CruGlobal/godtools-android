package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Locale

@Entity(
    tableName = "personalized_tool_order",
    primaryKeys = ["locale", "country", "tool"],
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["code"],
            childColumns = ["tool"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("tool"),
        Index("locale", "country", "order"),
    ]
)
internal data class PersonalizedToolOrderEntity(
    val locale: Locale,
    val country: String,
    val tool: String,
    val order: Int,
)
