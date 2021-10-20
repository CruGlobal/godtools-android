package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Followup
import org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_CREATE_TIME
import org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_DESTINATION
import org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_EMAIL
import org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_LANGUAGE
import org.keynote.godtools.android.db.Contract.FollowupTable.COLUMN_NAME

internal object FollowupMapper : BaseMapper<Followup>() {
    override fun mapField(values: ContentValues, field: String, obj: Followup) {
        when (field) {
            COLUMN_NAME -> values.put(field, obj.name)
            COLUMN_EMAIL -> values.put(field, obj.email)
            COLUMN_LANGUAGE -> values.put(field, serialize(obj.languageCode))
            COLUMN_DESTINATION -> values.put(field, obj.destination)
            COLUMN_CREATE_TIME -> values.put(field, serialize(obj.createTime))
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Followup()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        name = c.getString(COLUMN_NAME)
        email = c.getString(COLUMN_EMAIL)
        languageCode = c.getLocale(COLUMN_LANGUAGE)
        destination = c.getLong(COLUMN_DESTINATION)
        createTime = getDate(c, COLUMN_CREATE_TIME)
    }
}
