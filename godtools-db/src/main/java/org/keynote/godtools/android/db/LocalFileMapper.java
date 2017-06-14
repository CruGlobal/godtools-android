package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.keynote.godtools.android.model.LocalFile;

import static org.keynote.godtools.android.db.Contract.LocalFileTable.COLUMN_NAME;

final class LocalFileMapper extends AbstractMapper<LocalFile> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final LocalFile file) {
        switch (field) {
            case COLUMN_NAME:
                values.put(field, file.getFileName());
                break;
            default:
                super.mapField(values, field, file);
                break;
        }
    }

    @NonNull
    @Override
    protected LocalFile newObject(@NonNull final Cursor c) {
        return new LocalFile();
    }

    @NonNull
    @Override
    public LocalFile toObject(@NonNull final Cursor c) {
        final LocalFile file = super.toObject(c);

        file.setFileName(getString(c, COLUMN_NAME, LocalFile.INVALID_FILE_NAME));

        return file;
    }
}
