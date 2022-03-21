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
internal interface TranslationDao {
    @Query("SELECT * FROM translations")
    suspend fun getAll(): List<TranslationRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(translation: TranslationRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(translations: List<TranslationAemImport>)

    @Delete
    suspend fun remove(translations: List<TranslationRef>)

    @Query("SELECT * FROM translations WHERE $TRANSLATION_KEY LIMIT 1")
    suspend fun find(tool: String, language: Locale, version: Int): TranslationRef?

    @Query("UPDATE translations SET processed = :processed WHERE $TRANSLATION_KEY")
    suspend fun markProcessed(tool: String, language: Locale, version: Int, processed: Boolean)
}
