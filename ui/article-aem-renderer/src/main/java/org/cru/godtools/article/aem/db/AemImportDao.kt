package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.AemImport.AemImportArticle

@Dao
interface AemImportDao {
    @get:WorkerThread
    @get:Query("SELECT * FROM aemImports")
    val all: List<AemImport>

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(import: AemImport)

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(imports: List<AemImport>)

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreArticles(aemImportArticle: List<AemImportArticle>)

    @WorkerThread
    @Query("UPDATE aemImports SET lastAccessed = :date WHERE uri = :aemImportUri")
    fun updateLastAccessed(aemImportUri: Uri, date: Date)

    @WorkerThread
    @Query("UPDATE aemImports SET lastProcessed = :date WHERE uri = :aemImportUri")
    fun updateLastProcessed(aemImportUri: Uri, date: Date)

    @WorkerThread
    @Query(
        """
        DELETE FROM aemImportArticles
        WHERE aemImportUri = :aemImportUri AND articleUri NOT IN (:currentArticleUris)
        """
    )
    fun removeOldArticles(aemImportUri: Uri, currentArticleUris: List<@JvmSuppressWildcards Uri>)

    @WorkerThread
    @Query(
        """
        DELETE FROM aemImports
        WHERE
            uri NOT IN (SELECT aemImportUri FROM translationAemImports) AND
            lastAccessed < :lastAccessedBefore
        """
    )
    fun removeOrphanedAemImports(lastAccessedBefore: Date)

    @Query("SELECT * FROM aemImports WHERE uri = :uri")
    suspend fun find(uri: Uri?): AemImport?
}
