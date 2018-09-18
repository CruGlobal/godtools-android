package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Transaction;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.TranslationRef;
import org.cru.godtools.model.Translation;

import java.util.List;
import java.util.Set;

@Dao
public abstract class TranslationRepository {
    @NonNull
    private final ArticleRoomDatabase mDb;

    TranslationRepository(@NonNull final ArticleRoomDatabase db) {
        mDb = db;
    }

    @WorkerThread
    public boolean isProcessed(@NonNull final Translation translation) {
        final TranslationRef transRef = mDb.translationDao().find(TranslationRef.Key.from(translation));
        return transRef != null && transRef.processed;
    }

    @Transaction
    @WorkerThread
    public boolean addAemImports(@NonNull final Translation translation, @NonNull final List<Uri> importUris) {
        final TranslationRef.Key translationKey = TranslationRef.Key.from(translation);
        if (translationKey == null) {
            return false;
        }

        // create translation ref if it doesn't exist already
        final TranslationRef translationRef = new TranslationRef(translationKey);
        mDb.translationDao().insertOrIgnore(translationRef);

        // create and link all AEM Import objects
        final List<AemImport> imports = Stream.of(importUris)
                .map(AemImport::new)
                .toList();
        final List<TranslationRef.TranslationAemImport> relations = Stream.of(imports)
                .map(i -> new TranslationRef.TranslationAemImport(translationKey, i))
                .toList();
        mDb.aemImportDao().insertOrIgnore(imports, relations);

        // mark translation as processed
        mDb.translationDao().markProcessed(translationKey, true);

        return true;
    }

    @Transaction
    @WorkerThread
    public void removeMissingTranslations(@NonNull final List<Translation> translationsToKeep) {
        final Set<TranslationRef.Key> keys = Stream.of(translationsToKeep)
                .map(TranslationRef.Key::from)
                .withoutNulls()
                .collect(Collectors.toSet());

        final List<TranslationRef> orphans = Stream.of(mDb.translationDao().getAll())
                .filterNot(t -> keys.contains(t.key))
                .toList();

        mDb.translationDao().remove(orphans);
    }
}
