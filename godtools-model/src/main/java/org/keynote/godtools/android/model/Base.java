package org.keynote.godtools.android.model;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;

import java.security.SecureRandom;
import java.util.Random;

public abstract class Base {
    public static final long INVALID_ID = -1;

    @SuppressLint("TrulyRandom")
    private static final Random RAND = new SecureRandom();

    @Nullable
    @JsonApiId
    private Long mId = INVALID_ID;
    @Nullable
    @JsonApiIgnore
    private Long mStashedId = null;

    public long getId() {
        return mId != null ? mId : INVALID_ID;
    }

    public void setId(@Nullable final Long id) {
        mId = id;
    }

    public void initNew() {
        do {
            mId = (long) (-1 * RAND.nextInt(Integer.MAX_VALUE));
        } while (mId >= INVALID_ID || mId < Integer.MIN_VALUE);
    }

    public void stashId() {
        mStashedId = mId;
        mId = null;
    }

    public void restoreId() {
        mId = mStashedId;
        mStashedId = null;
    }
}
