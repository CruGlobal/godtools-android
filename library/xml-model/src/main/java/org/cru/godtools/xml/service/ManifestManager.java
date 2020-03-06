package org.cru.godtools.xml.service;

import android.annotation.SuppressLint;
import android.content.Context;

import org.cru.godtools.model.Translation;
import org.cru.godtools.xml.model.Manifest;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class ManifestManager extends KotlinManifestManager {
    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static ManifestManager sInstance;

    @NonNull
    public static synchronized ManifestManager getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new ManifestManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private ManifestManager(@NonNull final Context context) {
        super(context);
    }

    @Nullable
    @WorkerThread
    public Manifest getLatestPublishedManifest(@NonNull final String toolCode, @NonNull final Locale locale) {
        final Translation translation = dao.getLatestTranslation(toolCode, locale, true, true).orElse(null);
        if (translation == null) {
            return null;
        }

        // update the last accessed time
        translation.updateLastAccessed();
        dao.update(translation, TranslationTable.COLUMN_LAST_ACCESSED);

        // return the manifest for this translation
        try {
            return getManifestBlocking(translation);
        } catch (InterruptedException e) {
            // set interrupted flag and return immediately
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
