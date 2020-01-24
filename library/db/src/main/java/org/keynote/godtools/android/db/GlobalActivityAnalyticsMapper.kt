package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable

internal object GlobalActivityAnalyticsMapper : BaseMapper<GlobalActivityAnalytics?>() {
    override fun mapField(values: ContentValues, field: String, globalActivityAnalytics: GlobalActivityAnalytics) {
        when (field) {
            GlobalActivityAnalyticsTable.COLUMN_USERS -> values.put(field, globalActivityAnalytics.users)
            GlobalActivityAnalyticsTable.COLUMN_COUNTRIES -> values.put(field, globalActivityAnalytics.countries)
            GlobalActivityAnalyticsTable.COLUMN_LAUNCHES -> values.put(field, globalActivityAnalytics.launches)
            GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS -> {
                values.put(field, globalActivityAnalytics.gospelPresentation)
            }
            else -> super.mapField(values, field, globalActivityAnalytics)
        }
    }

    override fun newObject(c: Cursor): GlobalActivityAnalytics = GlobalActivityAnalytics()

    override fun toObject(c: Cursor): GlobalActivityAnalytics {
        val globalActivityAnalytics = super.toObject(c)
        globalActivityAnalytics.users = getInt(c, GlobalActivityAnalyticsTable.COLUMN_USERS, 0)
        globalActivityAnalytics.countries = getInt(c, GlobalActivityAnalyticsTable.COLUMN_LAUNCHES, 0)
        globalActivityAnalytics.launches = getInt(c, GlobalActivityAnalyticsTable.COLUMN_LAUNCHES, 0)
        globalActivityAnalytics.gospelPresentation = getInt(
            c,
            GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS,
            0
        )
        return globalActivityAnalytics
    }
}
