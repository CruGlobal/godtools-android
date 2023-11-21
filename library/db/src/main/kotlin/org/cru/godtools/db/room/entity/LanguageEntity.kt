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
    val id: Long,
    val name: String?,
    @ColumnInfo(defaultValue = "false")
    val isAdded: Boolean = false,
) {
    constructor(language: Language) : this(
        id = language.id,
        code = language.code,
        name = language.name,
        isAdded = language.isAdded
    )

    fun toModel() = Language(code = code, name = name).also {
        it.id = id
        it.isAdded = isAdded
    }
}
