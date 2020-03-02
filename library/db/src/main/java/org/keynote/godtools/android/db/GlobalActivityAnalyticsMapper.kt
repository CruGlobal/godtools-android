package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getInt
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable

internal object GlobalActivityAnalyticsMapper : BaseMapper<GlobalActivityAnalytics>() {
    override fun mapField(values: ContentValues, field: String, analytics: GlobalActivityAnalytics) {
        when (field) {
            GlobalActivityAnalyticsTable.COLUMN_USERS -> values.put(field, analytics.users)
            GlobalActivityAnalyticsTable.COLUMN_COUNTRIES -> values.put(field, analytics.countries)
            GlobalActivityAnalyticsTable.COLUMN_LAUNCHES -> values.put(field, analytics.launches)
            GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS -> values.put(field, analytics.gospelPresentations)
            else -> super.mapField(values, field, analytics)
        }
    }

    override fun newObject(c: Cursor) = GlobalActivityAnalytics()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        users = c.getInt(GlobalActivityAnalyticsTable.COLUMN_USERS, 0)
        countries = c.getInt(GlobalActivityAnalyticsTable.COLUMN_COUNTRIES, 0)
        launches = c.getInt(GlobalActivityAnalyticsTable.COLUMN_LAUNCHES, 0)
        gospelPresentations = c.getInt(GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS, 0)
    }
}
