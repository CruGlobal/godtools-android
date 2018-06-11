package org.cru.godtools.model.event;

import android.support.annotation.NonNull;

/**
 * This event is fired when a tool is actually opened.
 */
public final class ToolUsedEvent {
    @NonNull
    private final String mToolCode;

    public ToolUsedEvent(@NonNull final String code) {
        mToolCode = code;
    }

    public String getToolCode() {
        return mToolCode;
    }
}
