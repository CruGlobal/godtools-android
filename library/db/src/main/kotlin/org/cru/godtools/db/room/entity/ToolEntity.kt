package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cru.godtools.model.Tool

@Entity(tableName = "tools")
internal class ToolEntity(
    val id: Long,
    @PrimaryKey
    val code: String,
    val type: Tool.Type,
    val name: String? = null,
    val category: String? = null,
    val description: String? = null,
    @ColumnInfo(defaultValue = "0")
    val shares: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val pendingShares: Int = 0,
    val bannerId: Long? = null,
    val detailsBannerId: Long? = null,
    val detailsBannerAnimationId: Long? = null,
    val detailsBannerYoutubeVideoId: String? = null,
    @ColumnInfo(defaultValue = "false")
    val isScreenShareDisabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val defaultOrder: Int = 0,
    @ColumnInfo(defaultValue = "${Int.MAX_VALUE}")
    val order: Int = Int.MAX_VALUE,
    val metatoolCode: String? = null,
    val defaultVariantCode: String? = null,
    @ColumnInfo(defaultValue = "false")
    val isFavorite: Boolean = false,
    @ColumnInfo(defaultValue = "false")
    val isHidden: Boolean = false,
    @ColumnInfo(defaultValue = "false")
    val isSpotlight: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val changedFields: String = "",
) {
    constructor(tool: Tool) : this(
        id = tool.id,
        code = tool.code.orEmpty(),
        type = tool.type,
        name = tool.name,
        category = tool.category,
        description = tool.description,
        shares = tool.shares,
        pendingShares = tool.pendingShares,
        bannerId = tool.bannerId,
        detailsBannerId = tool.detailsBannerId,
        detailsBannerAnimationId = tool.detailsBannerAnimationId,
        detailsBannerYoutubeVideoId = tool.detailsBannerYoutubeVideoId,
        isScreenShareDisabled = tool.isScreenShareDisabled,
        defaultOrder = tool.defaultOrder,
        order = tool.order,
        metatoolCode = tool.metatoolCode,
        defaultVariantCode = tool.defaultVariantCode,
        isFavorite = tool.isFavorite,
        isHidden = tool.isHidden,
        isSpotlight = tool.isSpotlight,
        changedFields = tool.changedFieldsStr,
    )

    fun toModel() = Tool().also {
        it.id = id
        it.code = code
        it.type = type
        it.name = name
        it.category = category
        it.description = description
        it.shares = shares
        it.pendingShares = pendingShares
        it.bannerId = bannerId
        it.detailsBannerId = detailsBannerId
        it.detailsBannerAnimationId = detailsBannerAnimationId
        it.detailsBannerYoutubeVideoId = detailsBannerYoutubeVideoId
        it.isScreenShareDisabled = isScreenShareDisabled
        it.defaultOrder = defaultOrder
        it.order = order
        it.metatoolCode = metatoolCode
        it.defaultVariantCode = defaultVariantCode
        it.isFavorite = isFavorite
        it.isHidden = isHidden
        it.isSpotlight = isSpotlight
        it.changedFieldsStr = changedFields
    }
}
