package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.cru.godtools.model.Language;
import org.cru.godtools.model.Translation;

import androidx.annotation.NonNull;

import static org.cru.godtools.model.Translation.DEFAULT_LAST_ACCESSED;
import static org.cru.godtools.model.Translation.DEFAULT_PUBLISHED;
import static org.cru.godtools.model.Translation.DEFAULT_VERSION;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DESCRIPTION;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DOWNLOADED;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_LANGUAGE;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_LAST_ACCESSED;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_MANIFEST;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_NAME;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_PUBLISHED;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_TAGLINE;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_TOOL;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_VERSION;

final class TranslationMapper extends BaseMapper<Translation> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Translation translation) {
        switch (field) {
            case COLUMN_TOOL:
                values.put(field, translation.getToolCode());
                break;
            case COLUMN_LANGUAGE:
                values.put(field, serialize(translation.getLanguageCode()));
                break;
            case COLUMN_VERSION:
                values.put(field, translation.getVersion());
                break;
            case COLUMN_NAME:
                values.put(field, translation.getName());
                break;
            case COLUMN_DESCRIPTION:
                values.put(field, translation.getDescription());
                break;
            case COLUMN_TAGLINE:
                values.put(field, translation.getTagline());
                break;
            case COLUMN_MANIFEST:
                values.put(field, translation.getManifestFileName());
                break;
            case COLUMN_PUBLISHED:
                values.put(field, translation.isPublished());
                break;
            case COLUMN_DOWNLOADED:
                values.put(field, translation.isDownloaded());
                break;
            case COLUMN_LAST_ACCESSED:
                values.put(field, serialize(translation.getLastAccessed()));
                break;
            default:
                super.mapField(values, field, translation);
                break;
        }
    }

    @NonNull
    @Override
    protected Translation newObject(@NonNull final Cursor c) {
        return new Translation();
    }

    @NonNull
    @Override
    public Translation toObject(@NonNull final Cursor c) {
        final Translation translation = super.toObject(c);

        translation.setToolCode(getString(c, COLUMN_TOOL));
        translation.setLanguageCode(getLocale(c, COLUMN_LANGUAGE, Language.INVALID_CODE));
        translation.setVersion(getInt(c, COLUMN_VERSION, DEFAULT_VERSION));
        translation.setName(getString(c, COLUMN_NAME, null));
        translation.setDescription(getString(c, COLUMN_DESCRIPTION, null));
        translation.setTagline(getString(c, COLUMN_TAGLINE, null));
        translation.setManifestFileName(getString(c, COLUMN_MANIFEST, null));
        translation.setPublished(getBool(c, COLUMN_PUBLISHED, DEFAULT_PUBLISHED));
        translation.setDownloaded(getBool(c, COLUMN_DOWNLOADED, false));
        translation.setLastAccessed(getDate(c, COLUMN_LAST_ACCESSED, DEFAULT_LAST_ACCESSED));

        return translation;
    }
}
