package org.godtools.uiarticles_aem_renderer.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Date;

/**
 * This class represents the article table
 *
 * @author Gyasi Story
 */
@Entity(tableName = "article_table", indices = )
public class Article {
    /**
     * Unique Identifier for the article table
     */
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    public int mId;

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
    public String mDateCreated;

    /**
     * The date the article was last updated. If no
     * data exist should be set to date created.
     */
    @ColumnInfo(name = "date_updated")
    public String mDateUpdated;
}
