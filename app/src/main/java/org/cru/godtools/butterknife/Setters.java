package org.cru.godtools.butterknife;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

import butterknife.Setter;

public final class Setters {
    public static final Setter<ImageView, ColorStateList> TINT_LIST = (view, tintList, index) -> {
        final Drawable drawable = DrawableCompat.wrap(view.getDrawable());
        DrawableCompat.setTintList(drawable, tintList);
        view.setImageDrawable(drawable);
    };
}
