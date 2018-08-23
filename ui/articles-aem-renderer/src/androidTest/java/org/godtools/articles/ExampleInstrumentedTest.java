package org.godtools.articles;

import android.support.test.runner.AndroidJUnit4;

import org.godtools.articles.aem.db.ArticleRepository;
import org.godtools.articles.aem.db.ArticleRoomDatabase;
import org.godtools.articles.aem.db.AttachmentRepository;
import org.godtools.articles.aem.db.ManifestAssociationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private ArticleRepository mArticleDao;
    private AttachmentRepository mAttachmentDao;
    private ManifestAssociationRepository mAssociationDao;
    private ArticleRoomDatabase db;


    @Before
    public void createDb() {
//        Context context = InstrumentationRegistry.getTargetContext();
//        db = Room.inMemoryDatabaseBuilder(context,
// ArticleRoomDatabase.class).build();
//        mArticleDao = new ArticleRepository(g);
//        mAttachmentDao = db.mAttachmentDao();
//        mAssociationDao = db.mManifestAssociationDao();

    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//
//        List<Article> articles = new ArrayList<>();
//
//        for (int i = 0; i < 12; i++) {
//            Article article = new Article();
//
//
//        }

//        List<Article> savedArticles = mArticleDao.getAllArticles();

//        for (Article article : savedArticles) {
//            // Create an attachment for each
//            Attachment attachment = new Attachment();
//            attachment.mArticleKey = article.mId;
//            attachment.mAttachmentUrl = "https://i.pinimg.com/originals/b9/9e/a0/b99ea0abc5e6bf5f5c67c1797791cc82.jpg";
//            mAttachmentDao.insertAttachment(attachment);
//
//            // Create Manifest Association
//            ManifestAssociation association = new ManifestAssociation();
//            association.mArticleId = article.mId;
//            association.mManifestId = String.valueOf(article.mId % 3);  // just need to only have three versions
//            association.mManifestName = String.format("Manifest # %s", association.mManifestId);
//            mAssociationDao.insertAssociation(association);
//        }

        //verify that we are getting bat the Artcles
//        Boolean allAttachmentsSaved = true;
//
//        for (Article article : savedArticles) {
//            if (mAttachmentDao.getAttachmentsByArticle(article.mId) == null) {
//                allAttachmentsSaved = false;
//                break;
//            }
//        }
//
//        // verify that all of the Articles are retr
//
//        assert (savedArticles.size() > 0 & allAttachmentsSaved && mAssociationDao
//                .getArticlesByManifestID("0").size() > 0 && mAssociationDao
//                .getAssociationByManifestID("1").size() > 0);
    }
}
