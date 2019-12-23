package org.cru.godtools.databinding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_height")
internal fun View.setLayoutDimensions(h: Int) {
    layoutParams = layoutParams.apply { height = h }
}
