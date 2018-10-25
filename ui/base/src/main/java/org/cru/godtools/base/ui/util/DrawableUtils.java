package org.cru.godtools.base.ui.util;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

public class DrawableUtils {
    @Nullable
    public static Drawable tint(@Nullable final Drawable drawable, @ColorInt final int color) {
        if (drawable != null) {
            final Drawable out = DrawableCompat.wrap(drawable).mutate();
            DrawableCompat.setTint(out, color);
            return out;
        }
        return null;
    }
}
