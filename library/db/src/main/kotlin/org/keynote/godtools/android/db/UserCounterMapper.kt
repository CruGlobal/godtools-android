package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getDouble
import org.ccci.gto.android.common.util.database.getInt
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.UserCounter
import org.keynote.godtools.android.db.Contract.UserCounterTable.COLUMN_COUNT
import org.keynote.godtools.android.db.Contract.UserCounterTable.COLUMN_COUNTER_ID
import org.keynote.godtools.android.db.Contract.UserCounterTable.COLUMN_DECAYED_COUNT
import org.keynote.godtools.android.db.Contract.UserCounterTable.COLUMN_DELTA

internal object UserCounterMapper : AbstractMapper<UserCounter>() {
    override fun mapField(values: ContentValues, field: String, obj: UserCounter) {
        when (field) {
            COLUMN_COUNTER_ID -> values.put(field, obj.id)
            COLUMN_COUNT -> values.put(field, obj.apiCount)
            COLUMN_DECAYED_COUNT -> values.put(field, obj.apiDecayedCount)
            COLUMN_DELTA -> values.put(field, obj.delta)
            else -> super.mapField(values, field, obj)
        }
    }


    override fun newObject(c: Cursor) = UserCounter(c.getString(COLUMN_COUNTER_ID).orEmpty())
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        apiCount = c.getInt(COLUMN_COUNT, 0)
        apiDecayedCount = c.getDouble(COLUMN_DECAYED_COUNT, 0.0)
        delta = c.getInt(COLUMN_DELTA, 0)
    }
}
