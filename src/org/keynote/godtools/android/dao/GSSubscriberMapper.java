package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.keynote.godtools.android.api.GSSubscriber;

import org.keynote.godtools.android.dao.DBContract.GSSubscriberTable;

/**
 * Created by dsgoers on 3/31/16.
 */
public class GSSubscriberMapper extends AbstractMapper<GSSubscriber> {
    @NonNull
    @Override
    protected GSSubscriber newObject(@NonNull Cursor c) {
        return new GSSubscriber();
    }

    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final GSSubscriber obj) {

        switch(field) {
            case GSSubscriberTable.COL_ROUTE_ID:
                values.put(field, obj.getRouteId());
                break;
            case GSSubscriberTable.COL_LANGUAGE_CODE:
                values.put(field, obj.getLanguageCode());
                break;
            case GSSubscriberTable.COL_FIRST_NAME:
                values.put(field, obj.getFirstName());
                break;
            case GSSubscriberTable.COL_LAST_NAME:
                values.put(field, obj.getLastName());
                break;
            case GSSubscriberTable.COL_EMAIL:
                values.put(field, obj.getEmail());
                break;
            case GSSubscriberTable.COL_CREATED_TIMESTAMP:
                values.put(field, serialize(obj.getCreatedTimestamp()));
                break;
            default:
                super.mapField(values, field, obj);
        }


    }
}
