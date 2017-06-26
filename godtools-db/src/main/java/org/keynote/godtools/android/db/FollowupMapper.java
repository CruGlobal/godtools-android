package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.cru.godtools.model.Followup;

import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_DESTINATION;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_EMAIL;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_LANGUAGE;
import static org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_NAME;

final class FollowupMapper extends BaseMapper<Followup> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Followup followup) {
        switch (field) {
            case COLUMN_NAME:
                values.put(field, followup.getName());
                break;
            case COLUMN_EMAIL:
                values.put(field, followup.getEmail());
                break;
            case COLUMN_LANGUAGE:
                values.put(field, serialize(followup.getLanguageCode()));
                break;
            case COLUMN_DESTINATION:
                values.put(field, followup.getDestination());
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

        followup.setName(getString(c, COLUMN_NAME));
        followup.setEmail(getString(c, COLUMN_EMAIL));
        followup.setLanguageCode(getLocale(c, COLUMN_LANGUAGE));
        followup.setDestination(getLong(c, COLUMN_DESTINATION));

        return followup;
    }
}
