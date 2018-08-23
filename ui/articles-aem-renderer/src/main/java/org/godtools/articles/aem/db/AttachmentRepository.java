package org.godtools.articles.aem.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.godtools.articles.aem.model.Attachment;

import java.util.List;

/**
 * This class is used a connection point to the Attachment Data Access Object.
 *
 * @author Gyasi Story
 */
public class AttachmentRepository {

    private AttachmentDao mAttachmentDao;

    public AttachmentRepository(Application application) {
        ArticleRoomDatabase db = ArticleRoomDatabase.getINSTANCE(application);
        mAttachmentDao = db.mAttachmentDao();
    }

    /**
     * Insert for an Attachment. Any conflict will result in the new Attachment to replace
     * the existing.
     * @param attachment = the attachment to be  inserted.
     */
    public void insertAttachment(final Attachment attachment) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mAttachmentDao.insertAttachment(attachment));
    }

    /**
     * Deletes a collection of Attachments.
     *
     * @param attachments = A collection or single attachment to be deleted.
     */
    public void deleteAttachement(final Attachment... attachments) {
        /* This is called in AsyncTask to insure it doesn't run on UI thread */
        AsyncTask.execute(() -> mAttachmentDao.deleteAttachments(attachments));
    }

    /**
     * Fetches a Live Data Collection of Attachments based on the article it is associated with.
     *
     * @param articleId = the id of the article which is it refers to.
     * @return = Live Data Collection of Attachments
     */
    public LiveData<List<Attachment>> getAttachmentsByArticle(int articleId) {

        return mAttachmentDao.getAttachmentsByArticle(articleId);
    }
}
