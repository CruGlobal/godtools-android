package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.cru.godtools.model.Base;

import static org.keynote.godtools.android.db.Contract.BaseTable.COLUMN_ID;

abstract class BaseMapper<T extends Base> extends AbstractMapper<T> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field, @NonNull final T obj) {
        switch (field) {
            case COLUMN_ID:
                values.put(field, obj.getId());
                break;
            default:
                super.mapField(values, field, obj);
                break;
        }
    }

    @NonNull
    @Override
    public T toObject(@NonNull final Cursor c) {
        final T obj = super.toObject(c);

        obj.setId(getLong(c, COLUMN_ID, Base.INVALID_ID));

        return obj;
    }
}
