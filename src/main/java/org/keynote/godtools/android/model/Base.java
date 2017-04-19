package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId;

public abstract class Base {
    public static final long INVALID_ID = -1;

    @Nullable
    @JsonApiId
    private Long mId = INVALID_ID;

    public long getId() {
        return mId != null ? mId : INVALID_ID;
    }

    public void setId(@Nullable final Long id) {
        mId = id;
    }
}
