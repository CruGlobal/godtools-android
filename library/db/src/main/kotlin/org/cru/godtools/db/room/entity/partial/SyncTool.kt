package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Tool

class SyncTool(tool: Tool) {
    val id = tool.id
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
    val defaultOrder = tool.defaultOrder
    val metatoolCode = tool.metatoolCode
    val defaultVariantCode = tool.defaultVariantCode
    val isHidden = tool.isHidden
    val isSpotlight = tool.isSpotlight
}
