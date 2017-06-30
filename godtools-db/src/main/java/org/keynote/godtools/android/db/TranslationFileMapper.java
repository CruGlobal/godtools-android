package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.cru.godtools.model.LocalFile;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationFile;

import static org.keynote.godtools.android.db.Contract.TranslationFileTable.COLUMN_FILE;
import static org.keynote.godtools.android.db.Contract.TranslationFileTable.COLUMN_TRANSLATION;

final class TranslationFileMapper extends AbstractMapper<TranslationFile> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final TranslationFile file) {
        switch (field) {
            case COLUMN_TRANSLATION:
                values.put(field, file.getTranslationId());
                break;
            case COLUMN_FILE:
                values.put(field, file.getFileName());
                break;
            default:
                super.mapField(values, field, file);
                break;
        }
    }

    @NonNull
    @Override
    protected TranslationFile newObject(@NonNull final Cursor c) {
        return new TranslationFile();
    }

    @NonNull
    @Override
    public TranslationFile toObject(@NonNull final Cursor c) {
        final TranslationFile file = super.toObject(c);

        file.setTranslationId(getLong(c, COLUMN_TRANSLATION, Translation.INVALID_ID));
        file.setFileName(getString(c, COLUMN_FILE, LocalFile.INVALID_FILE_NAME));

        return file;
    }
}
