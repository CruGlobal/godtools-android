package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getInt
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Base
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.BaseTable.Companion.COLUMN_ID
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ADDED
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_BANNER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_CATEGORY
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_CODE
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DEFAULT_ORDER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DEFAULT_VARIANT
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DESCRIPTION
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER_ANIMATION
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER_YOUTUBE
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_HIDDEN
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_META_TOOL
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_NAME
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ORDER
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_PENDING_SHARES
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SCREEN_SHARE_DISABLED
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SHARES
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SPOTLIGHT
import org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_TYPE

internal object ToolMapper : AbstractMapper<Tool>() {
    override fun mapField(values: ContentValues, field: String, obj: Tool) {
        when (field) {
            COLUMN_ID -> values.put(field, obj.id)
            COLUMN_CODE -> values.put(field, obj.code)
            COLUMN_TYPE -> values.put(field, serialize(obj.type))
            COLUMN_NAME -> values.put(field, obj.name)
            COLUMN_DESCRIPTION -> values.put(field, obj.description)
            COLUMN_CATEGORY -> values.put(field, obj.category)
            COLUMN_SHARES -> values.put(field, obj.shares)
            COLUMN_PENDING_SHARES -> values.put(field, obj.pendingShares)
            COLUMN_BANNER -> values.put(field, obj.bannerId)
            COLUMN_DETAILS_BANNER -> values.put(field, obj.detailsBannerId)
            COLUMN_DETAILS_BANNER_ANIMATION -> values.put(field, obj.detailsBannerAnimationId)
            COLUMN_DETAILS_BANNER_YOUTUBE -> values.put(field, obj.detailsBannerYoutubeVideoId)
            COLUMN_SCREEN_SHARE_DISABLED -> values.put(field, obj.isScreenShareDisabled)
            COLUMN_DEFAULT_ORDER -> values.put(field, obj.defaultOrder)
            COLUMN_ORDER -> values.put(field, obj.order)
            COLUMN_META_TOOL -> values.put(field, obj.metatoolCode)
            COLUMN_DEFAULT_VARIANT -> values.put(field, obj.defaultVariantCode)
            COLUMN_ADDED -> values.put(field, obj.isFavorite)
            COLUMN_HIDDEN -> values.put(field, obj.isHidden)
            COLUMN_SPOTLIGHT -> values.put(field, obj.isSpotlight)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Tool(
        code = c.getString(COLUMN_CODE),
        type = getEnum(c, COLUMN_TYPE, Tool.Type::class.java, Tool.Type.DEFAULT)!!,
        name = c.getString(COLUMN_NAME),
        description = c.getString(COLUMN_DESCRIPTION),
        category = c.getString(COLUMN_CATEGORY),
    )
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        id = c.getLong(COLUMN_ID, Base.INVALID_ID)
        shares = c.getInt(COLUMN_SHARES, 0)
        pendingShares = c.getInt(COLUMN_PENDING_SHARES, 0)
        bannerId = c.getLong(COLUMN_BANNER)
        detailsBannerId = c.getLong(COLUMN_DETAILS_BANNER)
        detailsBannerAnimationId = c.getLong(COLUMN_DETAILS_BANNER_ANIMATION)
        detailsBannerYoutubeVideoId = c.getString(COLUMN_DETAILS_BANNER_YOUTUBE)
        defaultOrder = c.getInt(COLUMN_DEFAULT_ORDER, 0)
        order = c.getInt(COLUMN_ORDER, Int.MAX_VALUE)
        metatoolCode = c.getString(COLUMN_META_TOOL)
        defaultVariantCode = c.getString(COLUMN_DEFAULT_VARIANT)
        isFavorite = getBool(c, COLUMN_ADDED, false)
        isHidden = getBool(c, COLUMN_HIDDEN, false)
        isSpotlight = getBool(c, COLUMN_SPOTLIGHT, false)
        isScreenShareDisabled = getBool(c, COLUMN_SCREEN_SHARE_DISABLED, false)
    }
}
