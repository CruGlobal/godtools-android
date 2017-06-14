package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.keynote.godtools.android.business.GTLanguage;

import static org.keynote.godtools.android.dao.DBContract.GTLanguageTable.COL_CODE;
import static org.keynote.godtools.android.dao.DBContract.GTLanguageTable.COL_DOWNLOADED;
import static org.keynote.godtools.android.dao.DBContract.GTLanguageTable.COL_DRAFT;
import static org.keynote.godtools.android.dao.DBContract.GTLanguageTable.COL_NAME;

@Deprecated
public class GTLanguageMapper extends AbstractMapper<GTLanguage> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final GTLanguage language) {
        switch (field) {
            case COL_CODE:
                values.put(field, language.getLanguageCode());
                break;
            case COL_NAME:
                values.put(field, language.getLanguageName());
                break;
            case COL_DOWNLOADED:
                values.put(field, language.isDownloaded());
                break;
            case COL_DRAFT:
                values.put(field, language.isDraft());
                break;
            default:
                super.mapField(values, field, language);
                break;
        }
    }

    @NonNull
    @Override
    protected GTLanguage newObject(@NonNull final Cursor c) {
        return new GTLanguage();
    }

    @NonNull
    @Override
    public GTLanguage toObject(@NonNull Cursor c) {
        final GTLanguage language = super.toObject(c);

        language.setLanguageCode(getString(c, COL_CODE));
        language.setLanguageName(getString(c, COL_NAME));
        language.setDownloaded(getBool(c, COL_DOWNLOADED, false));

        return language;
    }
}
