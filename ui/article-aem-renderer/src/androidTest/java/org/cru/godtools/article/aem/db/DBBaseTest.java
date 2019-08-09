package org.cru.godtools.article.aem.db;

import android.net.Uri;

import org.cru.godtools.article.aem.model.Article;
import org.junit.Before;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public abstract class DBBaseTest extends BaseArticleRoomDatabaseIT {
    ArticleDao mArticleDao;

    @Before
    public void createDb() throws Exception {
        mArticleDao = mDb.articleDao();
        for (int i = 0; i < 12; i++) {
            Article article = new Article(Uri.parse("test:" + i + "aaslf" + i));
            article.setTitle(" The title = " + i);
            article.setContent("<p> The Body </>");
            mArticleDao.insertOrIgnore(article);
        }
    }
}
