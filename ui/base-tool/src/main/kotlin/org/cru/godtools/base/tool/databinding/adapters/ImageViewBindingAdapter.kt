package org.cru.godtools.base.tool.databinding.adapters

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter

@BindingAdapter("android:src")
internal fun ImageView.bindImageResource(@DrawableRes resId: Int) {
    if (resId != 0) {
        setImageResource(resId)
    } else {
        setImageDrawable(null)
    }
}
