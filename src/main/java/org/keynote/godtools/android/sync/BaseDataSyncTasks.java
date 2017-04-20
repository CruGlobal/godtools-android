package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.model.Language;

abstract class BaseDataSyncTasks extends BaseSyncTasks {
    private static final String[] API_FIELDS_LANGUAGE = {LanguageTable.COLUMN_LOCALE};

    BaseDataSyncTasks(@NonNull Context context) {
        super(context);
    }

    void storeLanguage(@NonNull final Language language) {
        mDao.updateOrInsert(language, API_FIELDS_LANGUAGE);
    }
}
