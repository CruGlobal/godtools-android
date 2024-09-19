package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import org.cru.godtools.model.Language

@Entity(tableName = "languages")
internal class LanguageEntity(
    @PrimaryKey
    val code: Locale,
    val name: String?,
    @ColumnInfo(defaultValue = "false")
    val isForcedName: Boolean = false,
    @ColumnInfo(defaultValue = "false")
    val isAdded: Boolean = false,
    val apiId: Long? = null,
) {
    constructor(language: Language) : this(
        code = language.code,
        name = language.name,
        isForcedName = language.isForcedName,
        isAdded = language.isAdded,
        apiId = language.apiId,
    )

    fun toModel() = Language(
        code = code,
        name = name,
        isForcedName = isForcedName,
        isAdded = isAdded,
        apiId = apiId,
    )
}
