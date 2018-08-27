package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;


import org.cru.godtools.articles.aem.model.Article;

import java.util.List;

/**
 * This class is use to go connection to the Article Data Access Object.
 *
 * @author Gyasi Story
 */
public class ArticleRepository {

    private final ArticleDao mArticleDao;
    private LiveData<List<Article>> mAllArticles;

    public ArticleRepository(Context context) {
        ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(context);
        mArticleDao = db.articleDao();
        mAllArticles = mArticleDao.getAllArticles();
    }

    /**
     * To call a list of all article stored in the database.
     *
     * @return = Live Data Collection of Articles
     */
    public LiveData<List<Article>> getAllArticles() {
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
