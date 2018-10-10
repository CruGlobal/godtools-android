package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * This class represents all attachment (Images, files, etc..) for
 * and article.
 *
 * @author Gyasi Story
 */
@Entity(tableName = "attachments")
public class Attachment {
    @NonNull
    @PrimaryKey
    public final Uri uri;

    /**
     *  This is the path to the local directory of the saved attachment.  This
     *  most likely will be set after the creation of the record.
     */
    @ColumnInfo(name = "attachment_file_path")
    public String mAttachmentFilePath;

    public Attachment(@NonNull final Uri uri) {
        this.uri = uri;
    }
}
