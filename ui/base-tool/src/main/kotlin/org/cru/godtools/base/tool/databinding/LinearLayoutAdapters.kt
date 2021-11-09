package org.cru.godtools.base.tool.databinding

import android.view.View
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_weight")
internal fun View.setLayoutWeight(weight: Float) {
    val params = layoutParams
    if (params is LinearLayout.LayoutParams && params.weight != weight) {
        params.weight = weight
        layoutParams = params
    }
}
