package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.net.Uri
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.AemImport
import org.cru.godtools.articles.aem.model.AemImport.AemImportArticle
import org.cru.godtools.articles.aem.model.TranslationRef.TranslationAemImport
import java.util.Date

@Dao
interface AemImportDao {
    @get:WorkerThread
    @get:Query("SELECT * FROM aemImports")
    val all: List<AemImport>

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(imports: List<AemImport>, translationRefs: List<TranslationAemImport>)

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreArticles(aemImportArticle: List<AemImportArticle>)

    @WorkerThread
    @Query("UPDATE aemImports SET lastProcessed = :date WHERE uri = :aemImportUri")
    fun updateLastProcessed(aemImportUri: Uri, date: Date)

    @WorkerThread
    @Query("""
        DELETE FROM aemImportArticles
        WHERE aemImportUri = :aemImportUri AND articleUri NOT IN (:currentArticleUris)""")
    fun removeOldArticles(aemImportUri: Uri, currentArticleUris: List<@JvmSuppressWildcards Uri>)

    @WorkerThread
    @Query("SELECT * FROM aemImports WHERE uri = :uri")
    fun find(uri: Uri?): AemImport?
}
