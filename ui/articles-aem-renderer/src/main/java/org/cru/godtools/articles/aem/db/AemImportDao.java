package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.TranslationRef;

import java.util.List;

@Dao
public abstract class AemImportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertOrIgnore(@NonNull List<AemImport> imports,
                                 @NonNull List<TranslationRef.TranslationAemImport> translationRefs);

    @Query("SELECT * FROM aemImports")
    public abstract List<AemImport> getAll();

    @Query("SELECT * FROM aemImports WHERE uri = :uri")
    public abstract AemImport find(Uri uri);
}
