package org.keynote.godtools.android.utils;

import android.support.annotation.Nullable;

/**
 * Strings Utility Class
 */
public final class Strings
{
    private Strings() {
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0;
    }
}
