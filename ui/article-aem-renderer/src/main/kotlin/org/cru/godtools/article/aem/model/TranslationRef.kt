package org.cru.godtools.article.aem.model

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale
import javax.annotation.concurrent.Immutable
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation

fun Translation.toTranslationRefKey(): TranslationRef.Key? {
    val tool = toolCode ?: return null
    val language = languageCode.takeUnless { it == Language.INVALID_CODE } ?: return null
    return TranslationRef.Key(tool, language, version)
}

@Entity(tableName = "translations")
class TranslationRef(
    @field:PrimaryKey
    @field:Embedded
    val key: Key,
) {
    var processed = false

    @Immutable
    data class Key(val tool: String, val language: Locale, val version: Int)

    @Immutable
    @Entity(
        tableName = "translationAemImports",
        primaryKeys = ["tool", "language", "version", "aemImportUri"],
        indices = [Index("aemImportUri")],
        foreignKeys = [
            ForeignKey(
                entity = TranslationRef::class,
                parentColumns = ["tool", "language", "version"],
                childColumns = ["tool", "language", "version"],
                onUpdate = ForeignKey.RESTRICT,
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = AemImport::class,
                parentColumns = ["uri"],
                childColumns = ["aemImportUri"],
                onUpdate = ForeignKey.RESTRICT,
                onDelete = ForeignKey.CASCADE
            )
        ]
    )
    class TranslationAemImport(@field:Embedded val translation: Key, val aemImportUri: Uri) {
        constructor(translation: Key, aemImport: AemImport) : this(translation, aemImport.uri)
    }
}
