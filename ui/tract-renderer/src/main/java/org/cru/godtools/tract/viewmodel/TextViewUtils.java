package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.ManifestViewUtils;
import org.cru.godtools.xml.model.Text;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

final class TextViewUtils {
    static void bind(@Nullable final Text text, @Nullable final TextView view) {
        bind(text, view, null, null);
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view, @Nullable @DimenRes Integer textSize,
                     @Nullable @ColorInt Integer defaultTextColor) {
        if (view != null) {
            // set default values if null
            if (textSize == null) {
                textSize = Text.textSize(text);
            }
            if (defaultTextColor == null) {
                defaultTextColor = Text.defaultTextColor(text);
            }

            view.setText(Text.getText(text));
            view.setTypeface(getTypeface(text, view.getContext()));

            final float size = view.getContext().getResources().getDimension(textSize);
            view.setTextSize(COMPLEX_UNIT_PX, (float) (size * Text.getTextScale(text)));

            if (text != null) {
                view.setTextColor(text.getTextColor(defaultTextColor));
            } else {
                view.setTextColor(defaultTextColor);
            }

            // set the alignment for the text
            view.setGravity((view.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) | Text.getTextAlign(text).mGravity);
        }
    }

    @Nullable
    private static Typeface getTypeface(@Nullable final Text text, @NonNull final Context context) {
        return text != null ? ManifestViewUtils.getTypeface(text.getManifest(), context) : null;
    }
}
