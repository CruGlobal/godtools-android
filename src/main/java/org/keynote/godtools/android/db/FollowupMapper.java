package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.keynote.godtools.android.model.Followup;

import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_CONTEXT_ID;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_GS_ACCESS_ID;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_GS_ACCESS_SECRET;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_GS_ROUTE_ID;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_ID;
import static org.keynote.godtools.android.model.Followup.DEFAULT_CONTEXT;

final class FollowupMapper extends AbstractMapper<Followup> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Followup followup) {
        switch (field) {
            case COLUMN_ID:
                values.put(field, followup.getId());
                break;
            case COLUMN_CONTEXT_ID:
                values.put(field, followup.getContextId());
                break;
            case COLUMN_GS_ROUTE_ID:
                values.put(field, followup.getGrowthSpacesRouteId());
                break;
            case COLUMN_GS_ACCESS_ID:
                values.put(field, followup.getGrowthSpacesAccessId());
                break;
            case COLUMN_GS_ACCESS_SECRET:
                values.put(field, followup.getGrowthSpacesAccessSecret());
                break;
            default:
                super.mapField(values, field, followup);
                break;
        }
    }

    @NonNull
    @Override
    protected Followup newObject(@NonNull final Cursor c) {
        return new Followup();
    }

    @NonNull
    @Override
    public Followup toObject(@NonNull final Cursor c) {
        final Followup followup = super.toObject(c);

        followup.setId(getLong(c, COLUMN_ID, Followup.INVALID_ID));
        followup.setContextId(getLong(c, COLUMN_CONTEXT_ID, DEFAULT_CONTEXT));
        followup.setGrowthSpacesRouteId(getLong(c, COLUMN_GS_ROUTE_ID, null));
        followup.setGrowthSpacesAccessId(getString(c, COLUMN_GS_ACCESS_ID, null));
        followup.setGrowthSpacesAccessSecret(getString(c, COLUMN_GS_ACCESS_SECRET, null));

        return followup;
    }
}
