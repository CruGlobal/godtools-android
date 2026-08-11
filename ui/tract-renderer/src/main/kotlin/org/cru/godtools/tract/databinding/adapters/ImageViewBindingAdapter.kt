package org.cru.godtools.tract.databinding.adapters

import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter

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
            else -> InsetDrawable(drawable, left, top, right, bottom)
        }
    )
}
