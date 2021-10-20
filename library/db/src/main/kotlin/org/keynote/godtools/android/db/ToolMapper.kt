package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getInt
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ADDED
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_BANNER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_CATEGORY
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_CODE
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DEFAULT_ORDER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DESCRIPTION
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_NAME
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ORDER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_OVERVIEW_VIDEO
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_PENDING_SHARES
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SHARES
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_TYPE

internal object ToolMapper : BaseMapper<Tool>() {
    override fun mapField(values: ContentValues, field: String, obj: Tool) {
        when (field) {
            COLUMN_CODE -> values.put(field, obj.code)
            COLUMN_TYPE -> values.put(field, serialize(obj.type))
            COLUMN_NAME -> values.put(field, obj.name)
            COLUMN_DESCRIPTION -> values.put(field, obj.description)
            COLUMN_CATEGORY -> values.put(field, obj.category)
            COLUMN_SHARES -> values.put(field, obj.shares)
            COLUMN_PENDING_SHARES -> values.put(field, obj.pendingShares)
            COLUMN_BANNER -> values.put(field, obj.bannerId)
            COLUMN_DETAILS_BANNER -> values.put(field, obj.detailsBannerId)
            COLUMN_OVERVIEW_VIDEO -> values.put(field, obj.overviewVideo)
            COLUMN_DEFAULT_ORDER -> values.put(field, obj.defaultOrder)
            COLUMN_ORDER -> values.put(field, obj.order)
            COLUMN_ADDED -> values.put(field, obj.isAdded)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Tool()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        code = c.getString(COLUMN_CODE)
        type = getEnum(c, COLUMN_TYPE, Tool.Type::class.java, Tool.Type.DEFAULT)!!
        name = c.getString(COLUMN_NAME)
        description = c.getString(COLUMN_DESCRIPTION)
        category = c.getString(COLUMN_CATEGORY)
        shares = c.getInt(COLUMN_SHARES, 0)
        pendingShares = c.getInt(COLUMN_PENDING_SHARES, 0)
        bannerId = c.getLong(COLUMN_BANNER)
        detailsBannerId = c.getLong(COLUMN_DETAILS_BANNER)
        overviewVideo = c.getString(COLUMN_OVERVIEW_VIDEO)
        defaultOrder = c.getInt(COLUMN_DEFAULT_ORDER, 0)
        order = c.getInt(COLUMN_ORDER, Int.MAX_VALUE)
        isAdded = getBool(c, COLUMN_ADDED, false)
    }
}
