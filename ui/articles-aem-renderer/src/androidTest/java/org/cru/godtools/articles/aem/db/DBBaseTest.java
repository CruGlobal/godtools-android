package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public abstract class DBBaseTest {

    ArticleDao mArticleDao;
    AttachmentDao mAttachmentDao;
    ManifestAssociationDao mAssociationDao;
    ArticleRoomDatabase db;
    List<Article> mSavedArticles = new ArrayList<>();
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

}
