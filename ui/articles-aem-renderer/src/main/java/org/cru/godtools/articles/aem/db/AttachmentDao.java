package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.cru.godtools.articles.aem.model.Attachment;


/**
 * This Data Access Object is the interface used to interact with the database.
 *
 * @author gyasistory
 */
@Dao
public interface AttachmentDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(@NonNull Attachment attachment);

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
}
