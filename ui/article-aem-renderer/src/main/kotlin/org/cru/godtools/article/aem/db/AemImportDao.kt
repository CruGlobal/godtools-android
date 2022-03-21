package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.AemImport.AemImportArticle

@Dao
internal interface AemImportDao {
    @Query("SELECT * FROM aemImports WHERE uri = :uri")
    suspend fun find(uri: Uri?): AemImport?
    @Query("SELECT * FROM aemImports")
    suspend fun getAll(): List<AemImport>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(aemImport: AemImport)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(imports: List<AemImport>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreArticles(aemImportArticle: List<AemImportArticle>)

    @Query("UPDATE aemImports SET lastAccessed = :date WHERE uri = :aemImportUri")
    suspend fun updateLastAccessed(aemImportUri: Uri, date: Date)
    @Query("UPDATE aemImports SET lastProcessed = :date WHERE uri = :aemImportUri")
    suspend fun updateLastProcessed(aemImportUri: Uri, date: Date)

    @Query(
        """
        DELETE FROM aemImportArticles
        WHERE aemImportUri = :aemImportUri AND articleUri NOT IN (:currentArticleUris)
        """
    )
    suspend fun removeOldArticles(aemImportUri: Uri, currentArticleUris: List<@JvmSuppressWildcards Uri>)
    @Query(
        """
        DELETE FROM aemImports
        WHERE
            uri NOT IN (SELECT aemImportUri FROM translationAemImports) AND
            lastAccessed < :lastAccessedBefore
        """
    )
    suspend fun removeOrphanedAemImports(lastAccessedBefore: Date)
}
