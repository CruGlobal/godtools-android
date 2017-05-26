package org.cru.godtools.tract.util;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;

public class DrawableUtils {
    @Nullable
    public static Drawable tint(@Nullable final Drawable drawable, @ColorInt final int color) {
        if (drawable != null) {
            final Drawable out = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(out, color);
            return out;
        }
        return null;
    }
}
