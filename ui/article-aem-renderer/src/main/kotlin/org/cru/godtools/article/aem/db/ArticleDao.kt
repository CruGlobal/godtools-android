package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Locale
import org.cru.godtools.article.aem.model.Article

private const val GET_ARTICLES_FROM = """
    translationAemImports AS t
    JOIN aemImportArticles AS i ON i.aemImportUri = t.aemImportUri
    JOIN articles AS a ON a.uri = i.articleUri"""
private const val GET_ARTICLES_WHERE = """
    t.tool = :tool AND t.language = :locale AND
    t.version = (
        SELECT version FROM translations
        WHERE tool = :tool AND language = :locale AND processed = 1 ORDER BY version DESC
    )"""

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE uri = :uri")
    suspend fun find(uri: Uri): Article?
    @AnyThread
    @Query("SELECT * FROM articles WHERE uri = :uri")
    fun findLiveData(uri: Uri): LiveData<Article?>

    @AnyThread
    @Query(
        """
        SELECT DISTINCT a.*
        FROM $GET_ARTICLES_FROM
        WHERE $GET_ARTICLES_WHERE
        ORDER BY a.title
        """
    )
    fun getArticles(tool: String, locale: Locale): LiveData<List<Article>>
    @AnyThread
    @Query(
        """
        SELECT DISTINCT a.*
        FROM $GET_ARTICLES_FROM
             JOIN articleTags AS tag ON tag.articleUri = a.uri
        WHERE $GET_ARTICLES_WHERE AND
            tag.tag IN (:tags)
        ORDER BY a.title
        """
    )
    fun getArticles(tool: String, locale: Locale, tags: List<@JvmSuppressWildcards String>): LiveData<List<Article>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(article: Article)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(articleResource: Article.ArticleResource)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreTags(tags: Collection<Article.Tag>)

    @Query(
        """
        UPDATE articles
        SET
            uuid = :uuid,
            title = :title,
            canonicalUri = :canonicalUri
        WHERE uri = :uri
        """
    )
    suspend fun update(uri: Uri, uuid: String, title: String, canonicalUri: Uri?)
    @Query("UPDATE articles SET contentUuid = :uuid, content = :content WHERE uri = :uri")
    suspend fun updateContent(uri: Uri, uuid: String?, content: String?)

    @Query("DELETE FROM articleTags WHERE articleUri = :articleUri")
    suspend fun removeAllTags(articleUri: Uri)
    @Query(
        """
        DELETE FROM articleResources
        WHERE articleUri = :articleUri AND resourceUri NOT IN (:currentResourceUris)
        """
    )
    suspend fun removeOldResources(articleUri: Uri, currentResourceUris: List<@JvmSuppressWildcards Uri>)
    @Query(
        """
        DELETE FROM articles
        WHERE uri NOT IN (SELECT articleUri FROM aemImportArticles)
        """
    )
    suspend fun removeOrphanedArticles()
}
