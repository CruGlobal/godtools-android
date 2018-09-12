package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class represents the article table
 *
 * @author Gyasi Story
 */
@Entity(tableName = "article_table")
public class Article {
    /**
     * Unique Identifier for the article table
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "article_key")
    public String mkey;

    /**
     * The title of the Article
     */
    @NonNull
    @ColumnInfo(name = "title")
    public String mTitle;

    /**
     * The main Content of the Article
     */
    @NonNull
    @ColumnInfo(name = "content")
    public String mContent;

    /**
     * The date the article was created
     */
    @NonNull
    @ColumnInfo(name = "date_created")
    public long mDateCreated;

    /**
     * The date the article was last updated. If no
     * data exist should be set to date created.
     */
    @ColumnInfo(name = "date_updated")
    public long mDateUpdated;

    @Ignore
    @Nullable
    public List<Attachment> parsedAttachments;

    public String getLastUpdatedFormatedString(){
        return new SimpleDateFormat("MM/DD/YYYY", Locale.getDefault())
                .format(new Date(mDateUpdated));
    }
}
