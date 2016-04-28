package org.keynote.godtools.android.utils;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class TypedValueUtils {
    public static float unapplyDimension(final int unit, final float value, @NonNull final DisplayMetrics metrics) {
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value / metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value / metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value / (metrics.xdpi * (1.0f / 72));
            case TypedValue.COMPLEX_UNIT_IN:
                return value / metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value / (metrics.xdpi * (1.0f / 25.4f));
        }
        return 0;
    }
}
