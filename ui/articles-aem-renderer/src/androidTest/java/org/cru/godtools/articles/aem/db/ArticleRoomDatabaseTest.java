package org.cru.godtools.articles.aem.db;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.support.test.runner.AndroidJUnit4;

import org.cru.godtools.articles.aem.model.Article;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArticleRoomDatabaseTest extends DBBaseTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void verifyGetAllArticles() {
        final LiveData<List<Article>> articles = mArticleDao.getAllArticles();
        articles.observeForever(o -> {});
        assertTrue("No article was saved.", articles.getValue().size() > 0);
    }
}
