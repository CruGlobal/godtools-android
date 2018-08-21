package org.godtools.uiarticles_aem_renderer.model;

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
    parentColumns = "_id", childColumns = "article_key"), indices = {@Index(value =
        {"attachment_url"})})
public class Attachment {

    /**
     *
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int mID;

    /**
     *
     */
    @NonNull
    @ColumnInfo(name = "article_key")
    public int mArticleKey;

    /**
     *
     */
    @NonNull
    @ColumnInfo(name = "attachment_url")
    public String mAttachmentUrl;

    /**
     *
     */
    @ColumnInfo(name = "attachment_file_path")
    public String mAttachmentFilePath;


}
