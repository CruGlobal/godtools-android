package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.model.Tool

private const val JSON_API_TYPE_TOOL_VIEWS = "view"
private const val JSON_TOOL_ID = "resource_id"
private const val JSON_QUANTITY = "quantity"

@JsonApiType(JSON_API_TYPE_TOOL_VIEWS)
class ToolViews(tool: Tool) {
    @JsonApiAttribute(JSON_TOOL_ID)
    private var toolId: Long? = tool.id
    @JsonApiIgnore
    var toolCode: String? = tool.code
    @JsonApiAttribute(JSON_QUANTITY)
    var quantity = tool.pendingShares
}
