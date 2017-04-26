package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.keynote.godtools.android.model.Resource;

import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_ADDED;
import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_NAME;

final class ResourceMapper extends BaseMapper<Resource> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Resource resource) {
        switch (field) {
            case COLUMN_NAME:
                values.put(field, resource.getName());
                break;
            case COLUMN_ADDED:
                values.put(field, resource.isAdded());
                break;
            default:
                super.mapField(values, field, resource);
                break;
        }
    }

    @NonNull
    @Override
    protected Resource newObject(@NonNull final Cursor c) {
        return new Resource();
    }

    @NonNull
    @Override
    public Resource toObject(@NonNull final Cursor c) {
        final Resource resource = super.toObject(c);

        resource.setName(getString(c, COLUMN_NAME, null));
        resource.setAdded(getBool(c, COLUMN_ADDED, false));

        return resource;
    }
}
