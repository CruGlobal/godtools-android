package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;

import org.ccci.gto.android.common.jsonapi.util.Includes;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ResourceTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.event.LanguageUpdateEvent;
import org.keynote.godtools.android.event.ResourceUpdateEvent;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

import java.util.List;

abstract class BaseDataSyncTasks extends BaseSyncTasks {
    private static final String[] API_FIELDS_LANGUAGE = {LanguageTable.COLUMN_LOCALE};
    private static final String[] API_FIELDS_RESOURCE = {ResourceTable.COLUMN_NAME};
    private static final String[] API_FIELDS_TRANSLATION =
            {TranslationTable.COLUMN_RESOURCE, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
                    TranslationTable.COLUMN_PUBLISHED};

    BaseDataSyncTasks(@NonNull Context context) {
        super(context);
    }

    void storeLanguage(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final Language language) {
        mDao.updateOrInsert(language, API_FIELDS_LANGUAGE);
        coalesceEvent(events, new LanguageUpdateEvent());
    }

    void storeResources(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final List<Resource> resources,
                        @Nullable final LongSparseArray<Resource> existing, @NonNull final Includes includes) {
        for (final Resource resource : resources) {
            if (existing != null) {
                existing.remove(resource.getId());
            }
            storeResource(events, resource, includes);
        }

        // prune any existing resources that weren't synced and aren't already added to the device
        if (existing != null) {
            for (int i = 0; i < existing.size(); i++) {
                final Resource resource = existing.valueAt(i);
                if (!resource.isAdded()) {
                    mDao.delete(resource);
                    coalesceEvent(events, new ResourceUpdateEvent());
                }
            }
        }
    }

    private void storeResource(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final Resource resource,
                               @NonNull final Includes includes) {
        mDao.updateOrInsert(resource, API_FIELDS_RESOURCE);
        coalesceEvent(events, new ResourceUpdateEvent());

        // persist any related included objects
        if (includes.include(Resource.JSON_LATEST_TRANSLATIONS)) {
            final List<Translation> translations = resource.getLatestTranslations();
            if (translations != null) {
                storeTranslations(events, translations, includes.descendant(Resource.JSON_LATEST_TRANSLATIONS));
            }
        }
    }

    private void storeTranslations(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                   @NonNull final List<Translation> translations, @NonNull final Includes includes) {
        for (final Translation translation : translations) {
            storeTranslation(events, translation, includes);
        }
    }

    private void storeTranslation(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                  @NonNull final Translation translation, @NonNull final Includes includes) {
        mDao.updateOrInsert(translation, API_FIELDS_TRANSLATION);
    }
}
