package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Tool

internal class SyncTool(tool: Tool) {
    val apiId = tool.apiId
    val code = tool.code.orEmpty()
    val type = tool.type
    val name = tool.name
    val category = tool.category
    val description = tool.description
    val shares = tool.shares
    val bannerId = tool.bannerId
    val detailsBannerId = tool.detailsBannerId
    val detailsBannerAnimationId = tool.detailsBannerAnimationId
    val detailsBannerYoutubeVideoId = tool.detailsBannerYoutubeVideoId
    val isScreenShareDisabled = tool.isScreenShareDisabled
    val defaultLocale = tool.defaultLocale
    val defaultOrder = tool.defaultOrder
    val metatoolCode = tool.metatoolCode
    val defaultVariantCode = tool.defaultVariantCode
    val isHidden = tool.isHidden
    val isSpotlight = tool.isSpotlight
}
