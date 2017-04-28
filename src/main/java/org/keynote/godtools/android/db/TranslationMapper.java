package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DOWNLOADED;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_LANGUAGE;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_PUBLISHED;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_RESOURCE;
import static org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_VERSION;
import static org.keynote.godtools.android.model.Translation.DEFAULT_PUBLISHED;
import static org.keynote.godtools.android.model.Translation.DEFAULT_VERSION;

final class TranslationMapper extends BaseMapper<Translation> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Translation resource) {
        switch (field) {
            case COLUMN_RESOURCE:
                values.put(field, resource.getResourceId());
                break;
            case COLUMN_LANGUAGE:
                values.put(field, serialize(resource.getLanguageCode()));
                break;
            case COLUMN_VERSION:
                values.put(field, resource.getVersion());
                break;
            case COLUMN_PUBLISHED:
                values.put(field, resource.isPublished());
                break;
            case COLUMN_DOWNLOADED:
                values.put(field, resource.isDownloaded());
                break;
            default:
                super.mapField(values, field, resource);
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
        final Translation resource = super.toObject(c);

        resource.setResourceId(getLong(c, COLUMN_RESOURCE, Resource.INVALID_ID));
        resource.setLanguageCode(getLocale(c, COLUMN_LANGUAGE, Language.INVALID_CODE));
        resource.setVersion(getInt(c, COLUMN_VERSION, DEFAULT_VERSION));
        resource.setPublished(getBool(c, COLUMN_PUBLISHED, DEFAULT_PUBLISHED));
        resource.setDownloaded(getBool(c, COLUMN_DOWNLOADED, false));

        return resource;
    }
}
