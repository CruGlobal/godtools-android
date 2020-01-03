package org.cru.godtools.article.databinding.adapter

import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.databinding.BindingAdapter
import org.cru.godtools.base.tool.model.view.TextViewUtils
import org.cru.godtools.xml.model.Text

@BindingAdapter(value = ["textNode", "android:textSize"])
internal fun TextView.setTextNode(text: Text?, @DimenRes textSize: Int?) =
    TextViewUtils.bind(text, this, textSize, null)

@BindingAdapter(value = ["textNode", "android:textSize", "defaultTextColor"])
internal fun TextView.setTextNode(text: Text?, @DimenRes textSize: Int?, @ColorInt defaultTextColor: Int?) =
    TextViewUtils.bind(text, this, textSize, defaultTextColor)
