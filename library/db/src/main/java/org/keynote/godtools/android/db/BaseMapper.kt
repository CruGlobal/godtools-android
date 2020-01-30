package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getLong
import org.cru.godtools.model.Base
import org.keynote.godtools.android.db.Contract.BaseTable

internal abstract class BaseMapper<T : Base> : AbstractMapper<T>() {
    override fun mapField(values: ContentValues, field: String, obj: T) {
        when (field) {
            BaseTable.COLUMN_ID -> values.put(field, obj.id)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun toObject(c: Cursor): T = super.toObject(c).apply {
        setId(c.getLong(BaseTable.COLUMN_ID, Base.INVALID_ID))
    }
}
