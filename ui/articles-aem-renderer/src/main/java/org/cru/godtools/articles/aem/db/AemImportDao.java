package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.TranslationRef;

import java.util.Date;
import java.util.List;

@Dao
public abstract class AemImportDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertOrIgnore(@NonNull List<AemImport> imports,
                                 @NonNull List<TranslationRef.TranslationAemImport> translationRefs);

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertOrIgnore(@NonNull final AemImport.AemImportArticle aemImportArticle);

    @WorkerThread
    @Query("UPDATE aemImports SET lastProcessed = :date WHERE uri = :aemImportUri")
    public abstract void updateLastProcessed(@NonNull Uri aemImportUri, @NonNull Date date);

    @WorkerThread
    @Query("DELETE FROM aemImportArticles WHERE aemImportUri = :aemImportUri AND articleUri NOT IN (:currentArticleUris)")
    public abstract void removeOldArticles(@NonNull Uri aemImportUri, @NonNull List<Uri> currentArticleUris);

    @Nullable
    @WorkerThread
    @Query("SELECT * FROM aemImports WHERE uri = :uri")
    public abstract AemImport find(Uri uri);

    @NonNull
    @WorkerThread
    @Query("SELECT * FROM aemImports")
    public abstract List<AemImport> getAll();
}
