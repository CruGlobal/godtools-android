package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.TranslationRef
import org.cru.godtools.articles.aem.model.TranslationRef.TranslationAemImport
import java.util.Locale

private const val TRANSLATION_KEY = "tool = :tool AND language = :language AND version = :version"

@Dao
@WorkerThread
internal interface TranslationDao {
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
