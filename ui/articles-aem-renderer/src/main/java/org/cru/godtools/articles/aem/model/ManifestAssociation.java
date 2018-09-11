package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * This class contain data for the connection of a Manifest
 * to an Article
 *
 * @author Gyasi Story
 */
@Entity(tableName = "manifest_association_table", foreignKeys = @ForeignKey(
        entity = Article.class, parentColumns = "article_key", childColumns = "article_key"))
public class ManifestAssociation {

    /**
     * The unique Identifier for this table
     */
    @PrimaryKey (autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    public int mId;

    /**
     * The descriptive name of the manifest
     */
    @NonNull
    @ColumnInfo(name = "manifest_name")
    public String mManifestName;

    /**
     * The unique Identifier of the Manifest
     */
    @NonNull
    @ColumnInfo(name = "manifest_key")
    public String mManifestId;


    /**
     * The Article associated to this association
     */
    @NonNull
    @ColumnInfo(name = "article_key")
    public String mArticleId;
}
