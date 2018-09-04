package org.cru.godtools.articles.aem.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.cru.godtools.articles.aem.db.ArticleRepository;
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.db.ManifestAssociationRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;

import java.util.List;

public class ArticleViewModel extends AndroidViewModel {
    private final ArticleRepository mArticleRepository;
    private final AttachmentRepository mAttchReposistory;
    private final ManifestAssociationRepository mManifestRepository;

    public ArticleViewModel(@NonNull Application application) {
        super(application);

        mArticleRepository = new ArticleRepository(application);
        mAttchReposistory = new AttachmentRepository(application);
        mManifestRepository = new ManifestAssociationRepository(application);
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

    public LiveData<List<Article>> getArticlesByManifest(String manifestID) {
        return mManifestRepository.getArticlesByManifestID(manifestID);
    }

    public LiveData<List<Attachment>> getAttachmentsByArticle(Article article) {
        return mAttchReposistory.getAttachmentsByArticle(article.mId);
    }
}
