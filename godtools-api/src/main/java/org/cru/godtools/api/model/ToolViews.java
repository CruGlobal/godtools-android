package org.cru.godtools.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;
import org.keynote.godtools.android.model.Tool;

import static org.cru.godtools.api.model.ToolViews.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public final class ToolViews {
    static final String JSON_API_TYPE = "view";
    public static final String JSON_TOOL_ID = "resource_id";
    public static final String JSON_QUANTITY = "quantity";

    @Nullable
    @JsonApiAttribute(name = JSON_TOOL_ID)
    private Long mToolId;
    @JsonApiAttribute(name = JSON_QUANTITY)
    private int mQuantity = 0;

    public ToolViews(@NonNull final Tool tool) {
        mToolId = tool.getId();
        mQuantity = tool.getPendingShares();
    }

    public long getToolId() {
        return mToolId != null ? mToolId : Tool.INVALID_ID;
    }

    public int getQuantity() {
        return mQuantity;
    }
}
