package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArticleDBTest implements LifecycleOwner {

    private static final String TAG = "ArticleDBTest";

    private ArticleDao mArticleDao;
    private AttachmentDao mAttachmentDao;
    private ManifestAssociationDao mAssociationDao;
    private ArticleRoomDatabase db;

    private Observer<List<Article>> articleObserver;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context,
                ArticleRoomDatabase.class).allowMainThreadQueries().build();
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
            try {
                article.mDateCreated = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz")
                        .parse("Fri Jun 08 2018 18:55:00 GMT+0000").getTime();
                article.mDateUpdated = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz")
                        .parse("Sat May 19 2018 00:23:39 GMT+0000").getTime();
            } catch (ParseException e) {
                Log.e(TAG, "useAppContext: ", e);
                article.mDateCreated = Calendar.getInstance().getTimeInMillis();
            }
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

    }


    private void associateToArticles(List<Article> savedArticles) {
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
    }

    private void getAllSavedArticles() {
        mArticleDao.getAllArticles().observe(this, articles -> {
            associateToArticles(articles);

            //verify that we are getting bat the Articles
            Boolean allAttachmentsSaved = false;

            for (Article article : articles) {
                if (mAttachmentDao.getAttachmentsByArticle(article.mId) == null) {
                    allAttachmentsSaved = false;
                    break;
                }

                allAttachmentsSaved = true;
            }

            // verify that all of the Articles are returned

            assert (Objects.requireNonNull(articles).size() > 0 &&
                    allAttachmentsSaved &&
                    Objects.requireNonNull(mAssociationDao
                            .getArticlesByManifestID("0").getValue()).size() > 0 &&
                    Objects.requireNonNull(mAssociationDao
                            .getAssociationByManifestID("1").getValue()).size() > 0);
        } );
    }


    @Override
    public Lifecycle getLifecycle() {
        return null;
    }
}
