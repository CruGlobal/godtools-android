package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.cru.godtools.model.GlobalActivityAnalytics;

import androidx.annotation.NonNull;

import static org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_COUNTRIES;
import static org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS;
import static org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_LAUNCHES;
import static org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_USERS;

public class GlobalActivityAnalyticsMapper extends BaseMapper<GlobalActivityAnalytics> {

    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final GlobalActivityAnalytics globalActivityAnalytics) {
        switch (field) {
            case COLUMN_USERS:
                values.put(field, globalActivityAnalytics.getUsers());
                break;
            case COLUMN_COUNTRIES:
                values.put(field, globalActivityAnalytics.getCountries());
                break;
            case COLUMN_LAUNCHES:
                values.put(field, globalActivityAnalytics.getLaunches());
                break;
            case COLUMN_GOSPEL_PRESENTATIONS:
                values.put(field, globalActivityAnalytics.getGospelPresentation());
                break;
            default:
                super.mapField(values, field, globalActivityAnalytics);
                break;
        }
    }

    @NonNull
    @Override
    protected GlobalActivityAnalytics newObject(@NonNull final Cursor c) {
        return new GlobalActivityAnalytics();
    }

    @NonNull
    @Override
    public GlobalActivityAnalytics toObject(@NonNull final Cursor c) {
        final GlobalActivityAnalytics globalActivityAnalytics = super.toObject(c);

        globalActivityAnalytics.setUsers(getInt(c, COLUMN_USERS, 0));
        globalActivityAnalytics.setCountries(getInt(c, COLUMN_LAUNCHES, 0));
        globalActivityAnalytics.setLaunches(getInt(c, COLUMN_LAUNCHES, 0));
        globalActivityAnalytics.setGospelPresentation(getInt(c, COLUMN_GOSPEL_PRESENTATIONS, 0));

        return globalActivityAnalytics;
    }
}
