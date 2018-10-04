package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import org.cru.godtools.articles.aem.model.Article;

import java.util.List;

/**
 *  The Data Access Object for the Article
 *
 *  @author Gyasi Story
 */
@Dao
public interface ArticleDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(@NonNull Article article);

    @WorkerThread
    @Query("UPDATE articles SET uuid = :uuid, title = :title WHERE uri = :uri")
    void update(@NonNull Uri uri, @NonNull String uuid, @NonNull String title);

    @WorkerThread
    @Query("UPDATE articles SET contentUuid = :uuid, content = :content WHERE uri = :uri")
    void updateContent(@NonNull Uri uri, @NonNull String uuid, @Nullable String content);

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

    @Query("SELECT * FROM articles WHERE uri = :uri")
    Article find(@NonNull Uri uri);

    /**
     *  The method to return all Articles.  User should use the ManifestAssociationDoa to
     *  get proper categories of the article.
     *
     * @return = Collection of all articles.
     */
    @Query("SELECT * FROM articles")
    LiveData<List<Article>> getAllArticles();

    //region Testable (Non Live Data)
    @VisibleForTesting
    @Query("SELECT * FROM articles")
    List<Article> getTestableAllArticles();
    //endregion
}
