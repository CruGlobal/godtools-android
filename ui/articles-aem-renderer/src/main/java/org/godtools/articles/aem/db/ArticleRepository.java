package org.godtools.articles.aem.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.godtools.articles.aem.model.Article;

import java.util.List;

/**
 *
 */
public class ArticleRepository {

    private ArticleDao mArticleDao;
    private LiveData<List<Article>> mAllArticles;

    public ArticleRepository(Application _application) {
        ArticleRoomDatabase db = ArticleRoomDatabase.getINSTANCE(_application);
        mArticleDao = db.mArticleDao();
        mAllArticles = mArticleDao.getAllArticles();
    }


    /**
     *
     * @return
     */
    public LiveData<List<Article>> getmAllArticles(){
        return mAllArticles;
    }

    public void insertArticle(final Article article){
        AsyncTask.execute(() -> mArticleDao.insertArticle(article));
    }

    public void deleteArticles(final Article... articles){
        AsyncTask.execute(() -> mArticleDao.deleteArticles(articles));
    }



}
