package org.godtools.articles.aem.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.godtools.articles.aem.model.Article;

import java.util.List;

/**
 * This class is use to go connection to the Article Data Access Object.
 *
 * @author Gyasi Story
 */
public class ArticleRepository {

    private ArticleDao mArticleDao;
    private LiveData<List<Article>> mAllArticles;

    public ArticleRepository(Application application) {
        ArticleRoomDatabase db = ArticleRoomDatabase.getINSTANCE(application);
        mArticleDao = db.mArticleDao();
        mAllArticles = mArticleDao.getAllArticles();
    }

    /**
     * To call a list of all article stored in the database.
     *
     * @return = Live Data Collection of Articles
     */
    public LiveData<List<Article>> getmAllArticles() {
        return mAllArticles;
    }

    /**
     * Insert a new Article.  Any conflict will result in the
     * Article being replace with the new instance.
     *
     * @param article = the Article to be inserted.
     */
    public void insertArticle(final Article article) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mArticleDao.insertArticle(article));
    }

    /**
     * Deletes a collection of Articles.
     *
     * @param articles = the collection or single article to be deleted.
     */
    public void deleteArticles(final Article... articles) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mArticleDao.deleteArticles(articles));
    }



}
