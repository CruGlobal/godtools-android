package org.cru.godtools.tract.databinding

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.compat.graphics.drawable.InsetDrawableCompat

@BindingAdapter("android:src", "srcInsetLeft", "srcInsetTop", "srcInsetRight", "srcInsetBottom")
internal fun ImageView.setInsetImageDrawable(
    drawable: Drawable?,
    insetLeft: Float?,
    insetTop: Float?,
    insetRight: Float?,
    insetBottom: Float?
) {
    val left = insetLeft?.toInt() ?: 0
    val top = insetTop?.toInt() ?: 0
    val right = insetRight?.toInt() ?: 0
    val bottom = insetBottom?.toInt() ?: 0
    setImageDrawable(
        when {
            drawable == null -> null
            left == 0 && top == 0 && right == 0 && bottom == 0 -> drawable
            else -> InsetDrawableCompat(drawable, left, top, right, bottom)
        }
    )
}

@BindingAdapter("android:src", "srcInset")
internal fun ImageView.setInsetImageDrawable(drawable: Drawable?, inset: Float?) {
    when {
        drawable == null || inset?.toInt() ?: 0 == 0 -> setImageDrawable(drawable)
        else -> setInsetImageDrawable(drawable, inset, inset, inset, inset)
    }
}
