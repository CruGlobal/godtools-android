package org.cru.godtools.articles.aem.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.cru.godtools.articles.aem.db.ArticleRepository;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;

import java.util.List;

public class ArticleViewModel extends AndroidViewModel {
    private final ArticleRepository mArticleRepository;
    private final AttachmentRepository mAttchReposistory;

    /**
     * This allows other models to get an instance of this ViewModel with out adding dependencies
     * to their .gradle file
     * @param activity FragmentActivity
     * @return this
     */
    public static ArticleViewModel getInstance(FragmentActivity activity) {
        return ViewModelProviders.of(activity).get(ArticleViewModel.class);
    }

    public ArticleViewModel(@NonNull Application application) {
        super(application);

        mArticleRepository = ArticleRoomDatabase.getInstance(application).articleRepository();
        mAttchReposistory = new AttachmentRepository(application);
    }

    public void insertArticle(Article article) {
        mArticleRepository.insertArticle(article);
    }

    public void deleteArticles(Article... articles) {
        mArticleRepository.deleteArticles(articles);
    }

    public LiveData<List<Article>> getArticles() {
        return mArticleRepository.getAllArticles();
    }

    public LiveData<List<Attachment>> getAttachmentsByArticle(Article article) {
        return mAttchReposistory.getAttachmentsByArticle(article.uri.toString());
    }
}
