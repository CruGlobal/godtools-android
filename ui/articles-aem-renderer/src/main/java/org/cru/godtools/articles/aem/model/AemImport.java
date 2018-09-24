package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;

import static org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS;

@Entity(tableName = "aemImports")
public class AemImport {
    private static final long STALE_AGE = DAY_IN_MS;

    @NonNull
    @PrimaryKey
    public final Uri uri;

    @NonNull
    public Date lastProcessed = new Date(0);

    public AemImport(@NonNull final Uri uri) {
        this.uri = uri;
    }

    public boolean isStale() {
        return lastProcessed.before(new Date(System.currentTimeMillis() - STALE_AGE));
    }

    @Entity(tableName = "aemImportArticles", primaryKeys = {"aemImportUri", "articleUri"}, foreignKeys = {
            @ForeignKey(entity = AemImport.class,
                    onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                    parentColumns = {"uri"}, childColumns = {"aemImportUri"}),
            @ForeignKey(entity = Article.class,
                    onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                    parentColumns = {"uri"}, childColumns = {"articleUri"})
    })
    public static class AemImportArticle {
        @NonNull
        public final Uri aemImportUri;

        @NonNull
        public final Uri articleUri;

        public AemImportArticle(@NonNull final Uri aemImportUri, @NonNull final Uri articleUri) {
            this.aemImportUri = aemImportUri;
            this.articleUri = articleUri;
        }

        public AemImportArticle(@NonNull final AemImport aemImport, @NonNull final Article article) {
            this(aemImport.uri, article.uri);
        }
    }
}
