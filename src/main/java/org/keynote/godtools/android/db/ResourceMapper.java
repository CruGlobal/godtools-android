package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.keynote.godtools.android.model.Resource;

import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_ADDED;
import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_COPYRIGHT;
import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_DESCRIPTION;
import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_NAME;
import static org.keynote.godtools.android.db.Contract.ResourceTable.COLUMN_SHARES;

final class ResourceMapper extends BaseMapper<Resource> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Resource resource) {
        switch (field) {
            case COLUMN_NAME:
                values.put(field, resource.getName());
                break;
            case COLUMN_DESCRIPTION:
                values.put(field, resource.getDescription());
                break;
            case COLUMN_SHARES:
                values.put(field, resource.getShares());
                break;
            case COLUMN_COPYRIGHT:
                values.put(field, resource.getCopyright());
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
        resource.setDescription(getString(c, COLUMN_DESCRIPTION, null));
        resource.setShares(getInt(c, COLUMN_SHARES, 0));
        resource.setCopyright(getString(c, COLUMN_COPYRIGHT, null));
        resource.setAdded(getBool(c, COLUMN_ADDED, false));

        return resource;
    }
}
