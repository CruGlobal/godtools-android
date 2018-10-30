package org.cru.godtools.articles.aem.model;

import android.net.Uri;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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
