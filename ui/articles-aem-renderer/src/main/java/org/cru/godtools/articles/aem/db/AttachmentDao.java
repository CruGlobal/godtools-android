package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.VisibleForTesting;

import org.cru.godtools.articles.aem.model.Attachment;

import java.util.List;


/**
 * This Data Access Object is the interface used to interact with the database.
 *
 * @author gyasistory
 */
@Dao
interface AttachmentDao {

    /**
     * Insertion of a unique Attachment.  If there is a conflict
     * the new data will replace the conflicting data.
     *
     * @param attachments = must be a unique Attachment
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAttachment(Attachment attachments);

    /**
     *
     * @param attachment
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAttachment(Attachment attachment);

    /**
     * Deletion of Attachments.  This can be one Attachment or
     * a collection.
     *
     * @param attachments = a single or group of attachments.
     */
    @Delete
    void deleteAttachments(Attachment... attachments);

    /**
     * To obtain a collection of attachment that are associated with a
     * particular article.
     *
     * @param articleId = The unique Identifier of the article
     * @return = a list of Attachments.  Using live data to support in Live updates.
     */
    @Query("SELECT * FROM attachment_table WHERE article_key = :articleId")
    LiveData<List<Attachment>> getAttachmentsByArticle(String articleId);

    //region Testable (None Live Data)
    @VisibleForTesting()
    @Query("SELECT * FROM attachment_table WHERE article_key = :articleId")
    List<Attachment> getTestableAttachmentsByArticle(String articleId);
    //endregion

}
