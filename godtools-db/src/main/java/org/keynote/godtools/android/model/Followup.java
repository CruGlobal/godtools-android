package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@Deprecated
public class Followup {
    public static final long INVALID_ID = -1;
    public static final long DEFAULT_CONTEXT = -1;

    @SerializedName("followup_id")
    private long mId = INVALID_ID;
    private long contextId = DEFAULT_CONTEXT;

    @Nullable
    private Long growthSpacesRouteId;
    @Nullable
    private String growthSpacesAccessId;
    @Nullable
    private String growthSpacesAccessSecret;

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

    public long getContextId() {
        return contextId;
    }

    public void setContextId(final long id) {
        contextId = id;
    }

    public boolean isDefault() {
        return contextId == DEFAULT_CONTEXT;
    }

    public void setDefault(final boolean state) {
        if (state) {
            contextId = DEFAULT_CONTEXT;
        }
    }

    @Nullable
    public Long getGrowthSpacesRouteId() {
        return growthSpacesRouteId;
    }

    public void setGrowthSpacesRouteId(@Nullable final Long routeId) {
        growthSpacesRouteId = routeId;
    }

    @Nullable
    public String getGrowthSpacesAccessId() {
        return growthSpacesAccessId;
    }

    public void setGrowthSpacesAccessId(@Nullable final String accessId) {
        growthSpacesAccessId = accessId;
    }

    @Nullable
    public String getGrowthSpacesAccessSecret() {
        return growthSpacesAccessSecret;
    }

    public void setGrowthSpacesAccessSecret(@Nullable final String secret) {
        growthSpacesAccessSecret = secret;
    }
}
