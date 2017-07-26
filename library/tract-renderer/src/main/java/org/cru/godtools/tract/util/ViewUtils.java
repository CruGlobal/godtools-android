package org.cru.godtools.tract.util;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {
    public static int getTopOffset(@NonNull final ViewGroup root, @IdRes final int id) {
        final View descendant = root.findViewById(id);
        return descendant != null ? getTopOffset(root, descendant) : 0;
    }

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
