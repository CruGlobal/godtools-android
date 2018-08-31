package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.cru.godtools.articles.aem.service.support.ArticleParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArticleDBTest {
    private ArticleDao mArticleDao;
    private AttachmentDao mAttachmentDao;
    private ManifestAssociationDao mAssociationDao;
    private ArticleRoomDatabase db;
    private List<Article> mSavedArticles = new ArrayList<>();
    Context context;

    @Before
    public void createDb() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context,
                ArticleRoomDatabase.class).allowMainThreadQueries().build();
        mArticleDao = db.articleDao();
        mAttachmentDao = db.attachmentDao();
        mAssociationDao = db.manifestAssociationDao();
        for (int i = 0; i < 12; i++) {
            Article article = new Article();
            article.mkey = i + "aaslf" + i;
            article.mContent = "<p> The Body </>";
            article.mDateCreated = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz")
                    .parse("Fri Jun 08 2018 18:55:00 GMT+0000").getTime();
            article.mDateUpdated = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz")
                    .parse("Sat May 19 2018 00:23:39 GMT+0000").getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.getTime();
            calendar.setTimeInMillis(article.mDateCreated);
            Date date = new Date(article.mDateCreated);
            date.getYear();
            date.getDate();
            date.getHours();
            date.getTimezoneOffset();
            article.mTitle = " The title = " + i;
            mArticleDao.insertArticle(article);
        }
        mSavedArticles = mArticleDao.getTestableAllArticles();
        for (int i = 0; i < mSavedArticles.size(); i++) {
            ManifestAssociation association = new ManifestAssociation();
            association.mArticleId = mSavedArticles.get(i).mkey;
            association.mManifestName = "Manifest ID " + (i % 3);
            association.mManifestId = (i % 3) + "";
            mAssociationDao.insertAssociation(association);
            Attachment attachment = new Attachment();
            attachment.mArticleKey = String.valueOf(mSavedArticles.get(i).mkey);
            attachment.mAttachmentUrl =
                    "https://believeacts2blog.files.wordpress.com/2015/10/image16.jpg";
            mAttachmentDao.insertAttachment(attachment);
        }
    }

    @After
    public void closeDb() {
        db.close();
    }

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


    @Test
    public void verifyArticleParseLogic() {

        try {
            Uri url = Uri.parse("https://stage.cru.org/content/experience-fragments/questions_about_god/english.100.json");

            JSONObject jsonObject = new JSONObject(IOUtils.toString(
                    new URI(url.toString()))
            );

            HashMap<String, Object> results = new ArticleParser(jsonObject).execute();

            assertTrue(results.containsKey(ArticleParser.ATTACHMENT_LIST_KEY));
            assertTrue(results.containsKey(ArticleParser.ARTICLE_LIST_KEY));
        } catch (MalformedURLException | JSONException | URISyntaxException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void verifyAttachmentsAreSaved() {
        for (Article article : mSavedArticles) {
            for (Attachment attachment: mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                try {
                    AEMDownloadManger
                            .saveAttachmentToStorage(attachment, context);
                } catch (IOException e) {
                    fail("Data was not saved");
                }
            }
        }

        for (Article article: mSavedArticles){
            for (Attachment attachment: mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                assertFalse(attachment.mAttachmentFilePath != null);
            }
        }
    }
}




