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
    private List<String> mCategories = ImmutableList.of();

    @Ignore
    @NonNull
    public List<Attachment> mAttachments = ImmutableList.of();

    public Article(@NonNull final Uri uri) {
        this.uri = uri;
    }

    public void setCategories(@NonNull final List<String> categories) {
        mCategories = ImmutableList.copyOf(categories);
    }

    @NonNull
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public List<Category> getCategoryObjects() {
        return Stream.of(mCategories)
                .map(category -> new Category(this, category))
                .toList();
    }

    public String getLastUpdatedFormattedString() {
        String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                .format(new Date(mDateUpdated));

        return String.format("Updated: %s", date);
    }

    @Immutable
    @Entity(tableName = "categories", primaryKeys = {"articleUri", "category"},
            foreignKeys = {
                    @ForeignKey(entity = Article.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"articleUri"})
            })
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class Category {
        @NonNull
        public final Uri articleUri;

        @NonNull
        public final String category;

        public Category(@NonNull final Article article, @NonNull final String category) {
            this(article.uri, category);
        }

        public Category(@NonNull final Uri articleUri, @NonNull final String category) {
            this.articleUri = articleUri;
            this.category = category;
        }
    }

    @Immutable
    @Entity(tableName = "articleAttachments", primaryKeys = {"articleUri", "attachmentUri"},
            indices = {@Index("attachmentUri")},
            foreignKeys = {
                    @ForeignKey(entity = Article.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"articleUri"}),
                    @ForeignKey(entity = Attachment.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"attachmentUri"})
    })
    public static class ArticleAttachment {
        @NonNull
        public final Uri articleUri;

        @NonNull
        public final Uri attachmentUri;

        public ArticleAttachment(@NonNull final Uri articleUri, @NonNull final Uri attachmentUri) {
            this.articleUri = articleUri;
            this.attachmentUri = attachmentUri;
        }

        public ArticleAttachment(@NonNull final Article article, @NonNull final Attachment attachment) {
            this(article.uri, attachment.uri);
        }
    }
}
