package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.cru.godtools.model.Language;

import static org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_ADDED;
import static org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_CODE;
import static org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_NAME;

final class LanguageMapper extends BaseMapper<Language> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Language language) {
        switch (field) {
            case COLUMN_CODE:
                values.put(field, serialize(language.getCode()));
                break;
            case COLUMN_ADDED:
                values.put(field, language.isAdded());
                break;
            case COLUMN_NAME:
                values.put(field, language.getLanguageName());
                break;
            default:
                super.mapField(values, field, language);
                break;
        }
    }

    @NonNull
    @Override
    protected Language newObject(@NonNull final Cursor c) {
        return new Language();
    }

    @NonNull
    @Override
    public Language toObject(@NonNull final Cursor c) {
        final Language language = super.toObject(c);

        language.setCode(getLocale(c, COLUMN_CODE, Language.INVALID_CODE));
        language.setAdded(getBool(c, COLUMN_ADDED, false));
        language.setLanguageName(getString(c, COLUMN_NAME, null));

        return language;
    }
}
