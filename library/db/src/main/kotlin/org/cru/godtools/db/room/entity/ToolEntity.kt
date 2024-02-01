package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import org.cru.godtools.model.Tool

@Entity(tableName = "tools")
internal class ToolEntity(
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
    @ColumnInfo(defaultValue = "en")
    val defaultLocale: Locale = Tool.DEFAULT_DEFAULT_LOCALE,
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
    val apiId: Long? = null,
) {
    constructor(tool: Tool) : this(
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
        defaultLocale = tool.defaultLocale,
        defaultOrder = tool.defaultOrder,
        order = tool.order,
        metatoolCode = tool.metatoolCode,
        defaultVariantCode = tool.defaultVariantCode,
        isFavorite = tool.isFavorite,
        isHidden = tool.isHidden,
        isSpotlight = tool.isSpotlight,
        apiId = tool.apiId,
        changedFields = tool.changedFieldsStr,
    )

    fun toModel() = Tool(
        code = code,
        type = type,
        name = name,
        category = category,
        description = description,
        bannerId = bannerId,
        detailsBannerId = detailsBannerId,
        detailsBannerAnimationId = detailsBannerAnimationId,
        detailsBannerYoutubeVideoId = detailsBannerYoutubeVideoId,
        defaultLocale = defaultLocale,
        defaultOrder = defaultOrder,
        order = order,
        isFavorite = isFavorite,
        isHidden = isHidden,
        isSpotlight = isSpotlight,
        isScreenShareDisabled = isScreenShareDisabled,
        shares = shares,
        pendingShares = pendingShares,
        metatoolCode = metatoolCode,
        defaultVariantCode = defaultVariantCode,
        apiId = apiId,
        changedFieldsStr = changedFields,
    )
}
