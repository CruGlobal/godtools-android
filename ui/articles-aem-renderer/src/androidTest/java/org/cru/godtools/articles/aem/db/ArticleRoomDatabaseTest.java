package org.cru.godtools.articles.aem.db;

import android.support.test.runner.AndroidJUnit4;

import org.cru.godtools.articles.aem.model.Article;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArticleRoomDatabaseTest extends DBBaseTest {

    @Test
    public void verifyGetAllArticles() {
        assertTrue("No article was saved.", mSavedArticles.size() > 0);
    }

    @Test
    public void verifyArticleHasAttachment() {
        for (Article article : mSavedArticles) {
            assertTrue(String.format("Article %s has no attachment", article.mTitle),
                       mAttachmentDao.getTestableAttachmentsByArticle(article.mkey).size() > 0);
        }
    }

    @Test
    public void verifyManifestHasArticles() {
        assertTrue("Manifest 0 should have articles", mAssociationDao.getTestableArticlesByManifestID("0").size() > 0);
        assertTrue("Manifest 1 should have articles", mAssociationDao.getTestableArticlesByManifestID("1").size() > 0);
        assertTrue("Manifest 2 should have articles", mAssociationDao.getTestableArticlesByManifestID("2").size() > 0);
    }
}
