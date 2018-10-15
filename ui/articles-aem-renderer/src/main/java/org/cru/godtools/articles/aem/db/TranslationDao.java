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
interface TranslationDao {
    String TRANSLATION_KEY = "tool = :tool AND language = :language AND version = :version";

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(@NonNull TranslationRef translation);

    @Delete
    void remove(@NonNull List<TranslationRef> translations);

    @Nullable
    @Query("SELECT * FROM translations WHERE " + TRANSLATION_KEY + " LIMIT 1")
    TranslationRef find(String tool, Locale language, int version);

    @NonNull
    @Query("SELECT * FROM translations")
    List<TranslationRef> getAll();

    @Query("UPDATE translations SET processed = :processed WHERE " + TRANSLATION_KEY)
    void markProcessed(String tool, Locale language, int version, boolean processed);
}
