package org.godtools.uiarticles_aem_renderer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import org.godtools.uiarticles_aem_renderer.model.Article;

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
    LiveData<List<Article>> getmAllArticles(){
        return mAllArticles;
    }
}
