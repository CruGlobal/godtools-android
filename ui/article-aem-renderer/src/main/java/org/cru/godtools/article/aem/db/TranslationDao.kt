package org.cru.godtools.article.aem.db

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Locale
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.TranslationRef.TranslationAemImport

private const val TRANSLATION_KEY = "tool = :tool AND language = :language AND version = :version"

@Dao
@WorkerThread
interface TranslationDao {
    @get:Query("SELECT * FROM translations")
    val all: List<TranslationRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(translation: TranslationRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(translations: List<TranslationAemImport>)

    @Delete
    fun remove(translations: List<TranslationRef>)

    @Query("SELECT * FROM translations WHERE $TRANSLATION_KEY LIMIT 1")
    fun find(tool: String, language: Locale, version: Int): TranslationRef?

    @Query("UPDATE translations SET processed = :processed WHERE $TRANSLATION_KEY")
    fun markProcessed(tool: String, language: Locale, version: Int, processed: Boolean)
}
