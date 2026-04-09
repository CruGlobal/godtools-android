package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Tool

internal class SyncPersonalizedTool(tool: Tool) {
    val apiId = tool.apiId
    val code = tool.code.orEmpty()
}
