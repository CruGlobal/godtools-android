package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.Article;

import java.util.List;

/**
 * This class is use to go connection to the Article Data Access Object.
 *
 * @author Gyasi Story
 */
@Dao
public abstract class ArticleRepository {
    @NonNull
    private final ArticleRoomDatabase mDb;

    ArticleRepository(@NonNull final ArticleRoomDatabase db) {
        mDb = db;
    }

    /**
     * To call a list of all article stored in the database.
     *
     * @return = Live Data Collection of Articles
     */
    public LiveData<List<Article>> getAllArticles() {
        return mDb.articleDao().getAllArticles();
    }

    /**
     * Insert a new Article.  Any conflict will result in the
     * Article being replace with the new instance.
     *
     * @param article = the Article to be inserted.
     */
    public void insertArticle(final Article article) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mDb.articleDao().insertArticle(article));
    }

    /**
     * Deletes a collection of Articles.
     *
     * @param articles = the collection or single article to be deleted.
     */
    public void deleteArticles(final Article... articles) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mDb.articleDao().deleteArticles(articles));
    }
}
