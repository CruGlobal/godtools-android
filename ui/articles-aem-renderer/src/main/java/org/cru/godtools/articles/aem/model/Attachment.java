package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * This class represents all attachment (Images, files, etc..) for
 * and article.
 *
 * @author Gyasi Story
 */
@Entity(tableName = "attachments",
        primaryKeys = {"articleUri", "uri"},
        indices = {@Index("uri")},
        foreignKeys = {
                @ForeignKey(entity = Article.class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = {"uri"}, childColumns = {"articleUri"})
        })
public class Attachment {
    @NonNull
    public final Uri articleUri;

    @NonNull
    public final Uri uri;

    /**
     *  This is the path to the local directory of the saved attachment.  This
     *  most likely will be set after the creation of the record.
     */
    @ColumnInfo(name = "attachment_file_path")
    public String mAttachmentFilePath;

    public Attachment(@NonNull final Article article, @NonNull final Uri uri) {
        this(article.uri, uri);
    }

    public Attachment(@NonNull final Uri articleUri, @NonNull final Uri uri) {
        this.articleUri = articleUri;
        this.uri = uri;
    }
}
