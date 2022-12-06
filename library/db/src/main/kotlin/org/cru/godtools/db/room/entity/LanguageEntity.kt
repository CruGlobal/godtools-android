package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import org.cru.godtools.model.Language

@Entity(tableName = "languages")
internal class LanguageEntity(
    @PrimaryKey
    val code: Locale,
    val id: Long,
    val name: String?
) {
    constructor(language: Language) : this(id = language.id, code = language.code, name = language.name)

    fun toModel() = Language().apply {
        id = this@LanguageEntity.id
        code = this@LanguageEntity.code
        name = this@LanguageEntity.name
    }
}
