package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

public class Followup {
    public static final long INVALID_ID = -1;
    public static final long DEFAULT_CONTEXT = -1;

    private long mId = INVALID_ID;
    private long mContextId = DEFAULT_CONTEXT;

    @Nullable
    private Long mGrowthSpacesRouteId;
    @Nullable
    private String mGrowthSpacesAccessId;
    @Nullable
    private String mGrowthSpacesAccessSecret;

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

    public long getContextId() {
        return mContextId;
    }

    public void setContextId(final long id) {
        mContextId = id;
    }

    public boolean isDefault() {
        return mContextId == DEFAULT_CONTEXT;
    }

    public void setDefault(final boolean state) {
        if (state) {
            mContextId = DEFAULT_CONTEXT;
        }
    }

    @Nullable
    public Long getGrowthSpacesRouteId() {
        return mGrowthSpacesRouteId;
    }

    public void setGrowthSpacesRouteId(@Nullable final Long routeId) {
        mGrowthSpacesRouteId = routeId;
    }

    @Nullable
    public String getGrowthSpacesAccessId() {
        return mGrowthSpacesAccessId;
    }

    public void setGrowthSpacesAccessId(@Nullable final String accessId) {
        mGrowthSpacesAccessId = accessId;
    }

    @Nullable
    public String getGrowthSpacesAccessSecret() {
        return mGrowthSpacesAccessSecret;
    }

    public void setGrowthSpacesAccessSecret(@Nullable final String secret) {
        mGrowthSpacesAccessSecret = secret;
    }
}
