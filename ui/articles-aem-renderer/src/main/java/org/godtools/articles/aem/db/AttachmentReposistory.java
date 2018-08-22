package org.godtools.articles.aem.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.godtools.articles.aem.model.Attachment;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class AttachmentReposistory {

    private AttachmentDao mAttachmentDao;

    /**
     *
     * @param application
     */
    public AttachmentReposistory(Application application) {
        ArticleRoomDatabase db = ArticleRoomDatabase.getINSTANCE(application);
        mAttachmentDao = db.mAttachmentDao();
    }

    /**
     *
     * @param attachment
     */
    public void insertAttachment(final Attachment attachment){
        AsyncTask.execute(() -> mAttachmentDao.insertAttachment(attachment));
    }

    /**
     *
     * @param attachments
     */
    public void deleteAttachement(final Attachment... attachments){
        AsyncTask.execute(() -> mAttachmentDao.deleteAttachments(attachments));
    }


    //TODO: Convert to Live Data after Unit Testing
    /**
     *
     * @param articleId
     * @return
     */
    public LiveData<List<Attachment>> getAttachmentsByArticle(int articleId) throws ExecutionException, InterruptedException {
        return  new AttachmentByArticleAsync(mAttachmentDao).execute(articleId).get();
    }

    /**
     *
     */
    private static class AttachmentByArticleAsync extends AsyncTask<Integer, Void, LiveData<List<Attachment>>>{

        private AttachmentDao doa;

        /**
         *
         * @param mAttachmentDao
         */
        public AttachmentByArticleAsync(AttachmentDao mAttachmentDao) {
            this.doa = mAttachmentDao;
        }

        /**
         *
         * @param integers
         * @return
         */
        @Override
        protected LiveData<List<Attachment>> doInBackground(Integer... integers) {
            return doa.getAttachmentsByArticle(integers[0]);
        }
    }
}
