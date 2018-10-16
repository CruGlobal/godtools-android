package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

/**
 * This class represents the article table
 *
 * @author Gyasi Story
 */
@Entity(tableName = "articles")
public class Article {
    @NonNull
    @PrimaryKey
    public final Uri uri;

    /**
     * UUID of this article
     */
    @NonNull
    public String uuid = "";

    /**
     * The title of the Article
     */
    @NonNull
    public String title = "";

    /**
     * UUID of the article the last time content was cached
     */
    @Nullable
    public String contentUuid;

    /**
     * The cached HTML Content of the Article
     */
    @Nullable
    public String content;

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
    @NonNull
    private List<String> mTags = ImmutableList.of();

    @Ignore
    @NonNull
    public List<Resource> mResources = ImmutableList.of();

    public Article(@NonNull final Uri uri) {
        this.uri = uri;
    }

    public void setTags(@NonNull final List<String> tags) {
        mTags = ImmutableList.copyOf(tags);
    }

    @NonNull
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public List<Tag> getTagObjects() {
        return Stream.of(mTags)
                .map(tag -> new Tag(this, tag))
                .toList();
    }

    public String getLastUpdatedFormattedString() {
        String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                .format(new Date(mDateUpdated));

        return String.format("Updated: %s", date);
    }

    @Immutable
    @Entity(tableName = "articleTags", primaryKeys = {"articleUri", "tag"},
            foreignKeys = {
                    @ForeignKey(entity = Article.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"articleUri"})
            })
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class Tag {
        @NonNull
        public final Uri articleUri;

        @NonNull
        public final String tag;

        public Tag(@NonNull final Article article, @NonNull final String tag) {
            this(article.uri, tag);
        }

        public Tag(@NonNull final Uri articleUri, @NonNull final String tag) {
            this.articleUri = articleUri;
            this.tag = tag;
        }
    }

    @Immutable
    @Entity(tableName = "articleResources", primaryKeys = {"articleUri", "resourceUri"},
            indices = {@Index("resourceUri")},
            foreignKeys = {
                    @ForeignKey(entity = Article.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"articleUri"}),
                    @ForeignKey(entity = Resource.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"resourceUri"})
            })
    public static class ArticleResource {
        @NonNull
        public final Uri articleUri;

        @NonNull
        public final Uri resourceUri;

        public ArticleResource(@NonNull final Uri articleUri, @NonNull final Uri resourceUri) {
            this.articleUri = articleUri;
            this.resourceUri = resourceUri;
        }

        public ArticleResource(@NonNull final Article article, @NonNull final Resource resource) {
            this(article.uri, resource.getUri());
        }
    }
}
