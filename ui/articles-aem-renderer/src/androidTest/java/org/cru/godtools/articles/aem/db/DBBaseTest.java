package org.cru.godtools.articles.aem.db;

import android.net.Uri;

import org.cru.godtools.articles.aem.model.Article;
import org.junit.Before;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public abstract class DBBaseTest extends BaseArticleRoomDatabaseIT {

    ArticleDao mArticleDao;

    @Before
    public void createDb() throws Exception {
        mArticleDao = mDb.articleDao();
        for (int i = 0; i < 12; i++) {
            Article article = new Article(Uri.parse("test:" + i + "aaslf" + i));
            article.content = "<p> The Body </>";
            article.title = " The title = " + i;
            mArticleDao.insertOrIgnore(article);
        }
    }
}
