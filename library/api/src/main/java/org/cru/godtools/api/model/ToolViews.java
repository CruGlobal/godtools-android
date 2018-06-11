package org.cru.godtools.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;
import org.cru.godtools.model.Tool;

import static org.cru.godtools.api.model.ToolViews.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public final class ToolViews {
    static final String JSON_API_TYPE = "view";
    public static final String JSON_TOOL_ID = "resource_id";
    public static final String JSON_QUANTITY = "quantity";

    @Nullable
    @JsonApiAttribute(name = JSON_TOOL_ID)
    private Long mToolId;
    @Nullable
    @JsonApiIgnore
    private String mToolCode;
    @JsonApiAttribute(name = JSON_QUANTITY)
    private int mQuantity = 0;

    public ToolViews(@NonNull final Tool tool) {
        mToolId = tool.getId();
        mToolCode = tool.getCode();
        mQuantity = tool.getPendingShares();
    }

    @Nullable
    public String getToolCode() {
        return mToolCode;
    }

    public int getQuantity() {
        return mQuantity;
    }
}
