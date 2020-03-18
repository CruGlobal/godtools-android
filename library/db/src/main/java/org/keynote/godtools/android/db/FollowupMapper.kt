package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Followup
import org.keynote.godtools.android.db.Contract.FollowupTable

internal object FollowupMapper : BaseMapper<Followup>() {
    override fun mapField(values: ContentValues, field: String, obj: Followup) {
        when (field) {
            FollowupTable.COLUMN_NAME -> values.put(field, obj.name)
            FollowupTable.COLUMN_EMAIL -> values.put(field, obj.email)
            FollowupTable.COLUMN_LANGUAGE -> values.put(field, serialize(obj.languageCode))
            FollowupTable.COLUMN_DESTINATION -> values.put(field, obj.destination)
            FollowupTable.COLUMN_CREATE_TIME -> values.put(field, serialize(obj.createTime))
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Followup()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        name = c.getString(FollowupTable.COLUMN_NAME)
        email = c.getString(FollowupTable.COLUMN_EMAIL)
        languageCode = c.getLocale(FollowupTable.COLUMN_LANGUAGE)
        destination = c.getLong(FollowupTable.COLUMN_DESTINATION)
        createTime = getDate(c, FollowupTable.COLUMN_CREATE_TIME)
    }
}
