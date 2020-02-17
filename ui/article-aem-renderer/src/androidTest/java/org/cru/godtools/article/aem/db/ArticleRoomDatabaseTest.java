package org.cru.godtools.article.aem.db;

import android.net.Uri;

import org.cru.godtools.article.aem.model.Article;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static junit.framework.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArticleRoomDatabaseTest extends BaseArticleRoomDatabaseIT {
    ArticleDao mArticleDao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

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

    @Test
    public void verifyGetAllArticles() {
        final LiveData<List<Article>> articles = mArticleDao.getAllArticles();
        articles.observeForever(o -> {});
        assertTrue("No article was saved.", articles.getValue().size() > 0);
    }
}
