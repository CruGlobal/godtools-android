package org.cru.godtools.base.tool.databinding

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import org.cru.godtools.tool.model.Gravity.Horizontal

@BindingAdapter("android:layout_weight")
internal fun View.setLayoutWeight(weight: Float) {
    val params = layoutParams
    if (params is LinearLayout.LayoutParams && params.weight != weight) {
        params.weight = weight
        layoutParams = params
    }
}

@BindingAdapter("android:layout_gravity")
internal fun View.setLayoutGravity(gravity: Horizontal) {
    when (val lp = layoutParams) {
        is LinearLayout.LayoutParams -> {
            val updated = when (gravity) {
                Horizontal.START -> Gravity.START
                Horizontal.CENTER -> Gravity.CENTER_HORIZONTAL
                Horizontal.END -> Gravity.END
            } or (lp.gravity and Gravity.VERTICAL_GRAVITY_MASK)
            if (lp.gravity != updated) {
                lp.gravity = updated
                layoutParams = lp
            }
        }
    }
}
