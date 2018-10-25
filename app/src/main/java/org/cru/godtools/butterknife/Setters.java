package org.cru.godtools.butterknife;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.graphics.drawable.DrawableCompat;
import butterknife.Setter;

public final class Setters {
    public static final Setter<ImageView, ColorStateList> TINT_LIST = (view, tintList, index) -> {
        final Drawable drawable = DrawableCompat.wrap(view.getDrawable());
        DrawableCompat.setTintList(drawable, tintList);
        view.setImageDrawable(drawable);
    };
}
