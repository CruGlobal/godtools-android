package org.godtools.articles.aem.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.godtools.articles.aem.db.ArticleRepository;
import org.godtools.articles.aem.model.Article;

import java.util.List;

public class ArticleViewModel extends AndroidViewModel {

    private ArticleRepository mRepository;

    public ArticleViewModel(@NonNull Application application) {
        super(application);

        mRepository = new ArticleRepository(application);
    }

    public void insertArticle(Article article){
        mRepository.insertArticle(article);
    }


    public void deleteArticles(Article... articles){
        mRepository.deleteArticles(articles);
    }

    public LiveData<List<Article>> getArticles(){
        return mRepository.getmAllArticles();
    }
}
