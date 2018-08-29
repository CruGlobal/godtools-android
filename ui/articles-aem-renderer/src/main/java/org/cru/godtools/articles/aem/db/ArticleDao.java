package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.annotation.VisibleForTesting;


import org.cru.godtools.articles.aem.model.Article;

import java.util.List;

/**
 *  The Data Access Object for the Article
 *
 *  @author Gyasi Story
 */
@Dao
interface ArticleDao {
    /**
     *  The insert method for an article.  Any conflict in with stored data will result
     *  in the data being replaced.
     *
     * @param article = the article to be saved.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(Article article);

    /**
     *  The delete method for an article.  Can take in on or multiple task.
     *
     * @param articles = The collection of article to be deleted.
     */
    @Delete
    void deleteArticles(Article... articles);

    //todo: Convert to LiveData after Testing
    /**
     *  The method to return all Articles.  User should use the ManifestAssociationDoa to
     *  get proper categories of the article.
     *
     * @return = Collection of all articles.
     */
    @Query("SELECT * FROM article_table")
    LiveData<List<Article>> getAllArticles();

    //region Testable (Non Live Data)
    @VisibleForTesting()
    @Query("SELECT * FROM article_table")
    List<Article> getTestableAllArticles();

    @VisibleForTesting()
    @Query("SELECT * FROM article_table WHERE article_key = :key")
    Article getArticleByKey(String key);
    //endregion
}
