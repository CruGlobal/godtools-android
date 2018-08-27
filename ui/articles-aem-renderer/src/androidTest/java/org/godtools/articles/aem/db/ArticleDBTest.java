package org.godtools.articles.aem.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.godtools.articles.aem.model.Article;
import org.godtools.articles.aem.model.Attachment;
import org.godtools.articles.aem.model.ManifestAssociation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private ArticleDao mArticleDao;
    private AttachmentDao mAttachmentDao;
    private ManifestAssociationDao mAssociationDao;
    private ArticleRoomDatabase db;


    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context,
                ArticleRoomDatabase.class).build();
        mArticleDao = db.articleDao();
        mAttachmentDao = db.attachmentDao();
        mAssociationDao = db.manifestAssociationDao();

    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Article article = new Article();

            article.mContent = "<p> The Body </>";
            article.mDateCreated = Calendar.getInstance().getTimeInMillis() - (i * 1000);
            article.mDateUpdated = Calendar.getInstance().getTimeInMillis() - (i * 500);
            article.mTitle = " The title = " + i;


        }

        List<Article> savedArticles = mArticleDao.getAllArticles().getValue();


        for (Article article : savedArticles != null ? savedArticles : new ArrayList<Article>()) {
            // Create an attachment for each
            Attachment attachment = new Attachment();
            attachment.mArticleKey = article.mId;
            attachment.mAttachmentUrl = "https://i.pinimg.com/originals/b9/9e/a0/b99ea0abc5e6bf5f5c67c1797791cc82.jpg";
            mAttachmentDao.insertAttachment(attachment);

            // Create Manifest Association
            ManifestAssociation association = new ManifestAssociation();
            association.mArticleId = article.mId;
            association.mManifestId = String.valueOf(article.mId % 3);  // just need to only have three versions
            association.mManifestName = String.format("Manifest # %s", association.mManifestId);
            mAssociationDao.insertAssociation(association);
        }

        //verify that we are getting bat the Artcles
        Boolean allAttachmentsSaved = true;

        for (Article article : savedArticles != null ? savedArticles : new ArrayList<Article>()) {
            if (mAttachmentDao.getAttachmentsByArticle(article.mId) == null) {
                allAttachmentsSaved = false;
                break;
            }
        }

        // verify that all of the Articles are retr

        assert (Objects.requireNonNull(savedArticles).size() > 0 & allAttachmentsSaved &&
                Objects.requireNonNull(mAssociationDao
                .getArticlesByManifestID("0").getValue()).size() > 0 &&
                Objects.requireNonNull(mAssociationDao
                .getAssociationByManifestID("1").getValue()).size() > 0);
    }
}
