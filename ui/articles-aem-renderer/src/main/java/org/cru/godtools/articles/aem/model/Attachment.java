package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * This class represents all attachment (Images, files, etc..) for
 * and article.
 *
 * @author Gyasi Story
 */
@Entity(tableName = "attachment_table", foreignKeys = @ForeignKey(entity = Article.class,
    parentColumns = "article_key", childColumns = "article_key"), indices = {@Index(value =
        {"attachment_url"})})
public class Attachment {

    /**
     * Unique Identifier for Attachment table
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int mID;

    /**
     * the Foreign Key for the associated Article
     */
    @NonNull
    @ColumnInfo(name = "article_key")
    public String mArticleKey;

    /**
     *  The Url path fot the attachment
     */
    @NonNull
    @ColumnInfo(name = "attachment_url")
    public String mAttachmentUrl;

    /**
     *  This is the path to the local directory of the saved attachment.  This
     *  most likely will be set after the creation of the record.
     */
    @ColumnInfo(name = "attachment_file_path")
    public String mAttachmentFilePath;


}
