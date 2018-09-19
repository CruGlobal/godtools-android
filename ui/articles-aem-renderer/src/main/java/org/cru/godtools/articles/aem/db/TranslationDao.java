package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.articles.aem.model.TranslationRef;

import java.util.List;
import java.util.Locale;

@Dao
abstract class TranslationDao {
    private static final String TRANSLATION_KEY = "tool = :tool AND language = :language AND version = :version";

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertOrIgnore(@NonNull TranslationRef translation);

    @Delete
    abstract void remove(@NonNull List<TranslationRef> translations);

    @Nullable
    TranslationRef find(@Nullable final TranslationRef.Key key) {
        if (key == null) {
            return null;
        }

        return find(key.tool, key.language, key.version);
    }

    @Nullable
    @Query("SELECT * FROM translations WHERE " + TRANSLATION_KEY + " LIMIT 1")
    abstract TranslationRef find(String tool, Locale language, int version);

    @Query("SELECT * FROM translations")
    abstract List<TranslationRef> getAll();

    void markProcessed(@Nullable final TranslationRef.Key key, final boolean processed) {
        if (key == null) {
            return;
        }

        markProcessed(key.tool, key.language, key.version, processed);
    }

    @Query("UPDATE translations SET processed = :processed WHERE " + TRANSLATION_KEY)
    abstract void markProcessed(String tool, Locale language, int version, boolean processed);
}
