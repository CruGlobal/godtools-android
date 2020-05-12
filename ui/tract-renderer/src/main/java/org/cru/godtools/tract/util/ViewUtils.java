package org.cru.godtools.tract.util;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.TintableBackgroundView;
import androidx.core.view.ViewCompat;

public class ViewUtils {
    public static int getTopOffset(@NonNull final ViewGroup root, @NonNull final View descendant) {
        final Rect bounds = new Rect();
        descendant.getDrawingRect(bounds);
        root.offsetDescendantRectToMyCoords(descendant, bounds);
        return bounds.top;
    }

    public static void setBackgroundTint(@NonNull final View view, @ColorInt final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || view instanceof TintableBackgroundView) {
            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color));
        } else {
            // update background tint directly on the background drawable
            Drawable bkg = view.getBackground();
            if (bkg != null) {
                bkg = DrawableCompat.wrap(bkg).mutate();
                DrawableCompat.setTint(bkg, color);
                ViewCompat.setBackground(view, bkg);
            }
        }
    }
}
