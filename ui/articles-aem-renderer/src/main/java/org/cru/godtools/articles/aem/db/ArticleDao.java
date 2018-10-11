package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.cru.godtools.articles.aem.model.Article;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(@NonNull Collection<Article.Category> categories);

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(@NonNull Article.ArticleResource articleResource);

    @WorkerThread
    @Query("UPDATE articles SET uuid = :uuid, title = :title WHERE uri = :uri")
    void update(@NonNull Uri uri, @NonNull String uuid, @NonNull String title);

    @WorkerThread
    @Query("UPDATE articles SET contentUuid = :uuid, content = :content WHERE uri = :uri")
    void updateContent(@NonNull Uri uri, @Nullable String uuid, @Nullable String content);

    @WorkerThread
    @Query("DELETE FROM categories WHERE articleUri = :articleUri")
    void removeAllCategories(@NonNull Uri articleUri);

    @WorkerThread
    @Query("DELETE FROM articleResources " +
            "WHERE articleUri = :articleUri AND resourceUri NOT IN (:currentResourceUris)")
    void removeOldResources(@NonNull Uri articleUri, @NonNull List<Uri> currentResourceUris);

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

    @AnyThread
    @Query("SELECT DISTINCT a.* " +
            "FROM translationAemImports AS t " +
            "JOIN aemImportArticles AS i ON i.aemImportUri = t.aemImportUri " +
            "JOIN articles AS a ON a.uri = i.articleUri " +
            "WHERE t.tool = :tool AND t.language = :locale AND t.version = (" +
                "SELECT version FROM translations " +
                "WHERE tool = :tool AND language = :locale AND processed = 1 ORDER BY version DESC)")
    LiveData<List<Article>> getArticles(@NonNull String tool, @NonNull Locale locale);
}
