package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getInt
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_COUNTRIES
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_LAUNCHES
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable.COLUMN_USERS

internal object GlobalActivityAnalyticsMapper : BaseMapper<GlobalActivityAnalytics>() {
    override fun mapField(values: ContentValues, field: String, obj: GlobalActivityAnalytics) {
        when (field) {
            COLUMN_USERS -> values.put(field, obj.users)
            COLUMN_COUNTRIES -> values.put(field, obj.countries)
            COLUMN_LAUNCHES -> values.put(field, obj.launches)
            COLUMN_GOSPEL_PRESENTATIONS -> values.put(field, obj.gospelPresentations)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = GlobalActivityAnalytics()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        users = c.getInt(COLUMN_USERS, 0)
        countries = c.getInt(COLUMN_COUNTRIES, 0)
        launches = c.getInt(COLUMN_LAUNCHES, 0)
        gospelPresentations = c.getInt(COLUMN_GOSPEL_PRESENTATIONS, 0)
    }
}
