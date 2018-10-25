package org.cru.godtools.article.databinding.adapter;

import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.xml.model.Text;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

public final class TextViewAdapters {
    @BindingAdapter(value = {"textNode", "android:textSize"})
    public static void setText(@NonNull final TextView view, @Nullable final Text text,
                               @Nullable @DimenRes final Integer textSize) {
        TextViewUtils.bind(text, view, textSize, null);
    }

    @BindingAdapter(value = {"textNode", "android:textSize", "defaultTextColor"})
    public static void setText(@NonNull final TextView view, @Nullable final Text text,
                               @Nullable @DimenRes final Integer textSize,
                               @Nullable @ColorInt final Integer defaultTextColor) {
        TextViewUtils.bind(text, view, textSize, defaultTextColor);
    }
}
