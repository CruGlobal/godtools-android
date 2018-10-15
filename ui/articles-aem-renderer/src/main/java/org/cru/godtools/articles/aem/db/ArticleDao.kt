package org.cru.godtools.articles.aem.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.net.Uri
import android.support.annotation.AnyThread
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.Article
import java.util.Locale

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
    @get:Query("SELECT * FROM articles")
    val allArticles: LiveData<List<Article>>

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(article: Article)

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(categories: Collection<Article.Category>)

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(articleResource: Article.ArticleResource)

    @WorkerThread
    @Query("UPDATE articles SET uuid = :uuid, title = :title WHERE uri = :uri")
    fun update(uri: Uri, uuid: String, title: String)

    @WorkerThread
    @Query("UPDATE articles SET contentUuid = :uuid, content = :content WHERE uri = :uri")
    fun updateContent(uri: Uri, uuid: String?, content: String?)

    @WorkerThread
    @Query("DELETE FROM categories WHERE articleUri = :articleUri")
    fun removeAllCategories(articleUri: Uri)

    @WorkerThread
    @Query("""
        DELETE FROM articleResources
        WHERE articleUri = :articleUri AND resourceUri NOT IN (:currentResourceUris)""")
    fun removeOldResources(articleUri: Uri, currentResourceUris: List<@JvmSuppressWildcards Uri>)

    @WorkerThread
    @Query("SELECT * FROM articles WHERE uri = :uri")
    fun find(uri: Uri): Article?

    @AnyThread
    @Query("SELECT * FROM articles WHERE uri = :uri")
    fun findLiveData(uri: Uri): LiveData<Article>

    @AnyThread
    @Query("""
        SELECT DISTINCT a.*
        FROM $GET_ARTICLES_FROM
        WHERE $GET_ARTICLES_WHERE""")
    fun getArticles(tool: String, locale: Locale): LiveData<List<Article>>

    @AnyThread
    @Query("""
        SELECT DISTINCT a.*
        FROM $GET_ARTICLES_FROM
             JOIN categories AS c ON c.articleUri = a.uri
        WHERE $GET_ARTICLES_WHERE AND
            c.category = :category""")
    fun getArticles(tool: String, locale: Locale, category: String): LiveData<List<Article>>
}
