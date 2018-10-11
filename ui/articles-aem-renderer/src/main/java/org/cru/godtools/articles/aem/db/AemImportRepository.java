package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Transaction;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.Article;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
public abstract class AemImportRepository {
    @NonNull
    private final ArticleRoomDatabase mDb;

    AemImportRepository(@NonNull final ArticleRoomDatabase db) {
        mDb = db;
    }

    @Transaction
    @WorkerThread
    public void processAemImportSync(@NonNull final AemImport aemImport, @NonNull final List<Article> articles) {
        final ArticleDao articleDao = mDb.articleDao();
        final AemImportDao aemImportDao = mDb.aemImportDao();

        final List<Uri> articleUris = new ArrayList<>();
        for (final Article article : articles) {
            articleUris.add(article.uri);

            // insert/update article
            articleDao.insertOrIgnore(article);
            articleDao.update(article.uri, article.uuid, article.title);

            // associate the article with this AemImport
            aemImportDao.insertOrIgnore(new AemImport.AemImportArticle(aemImport, article));

            // replace all categories
            articleDao.removeAllCategories(article.uri);
            articleDao.insertOrIgnore(article.getCategoryObjects());
        }

        // remove any articles that are no longer part of the AemImport
        aemImportDao.removeOldArticles(aemImport.uri, articleUris);

        // update the last processed time
        aemImportDao.updateLastProcessed(aemImport.uri, new Date());
    }
}
