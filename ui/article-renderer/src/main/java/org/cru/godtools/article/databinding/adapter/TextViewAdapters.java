package org.cru.godtools.article.databinding.adapter;

import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.xml.model.Text;

public final class TextViewAdapters {
    @BindingAdapter(value = {"textNode", "android:textSize"}, requireAll = false)
    public static void setText(@NonNull final TextView view, @Nullable final Text text, @Nullable final Integer textSize) {
        TextViewUtils.bind(text, view, textSize, null);
    }
}
