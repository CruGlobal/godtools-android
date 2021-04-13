package org.cru.godtools.base.ui.databinding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_height")
internal fun View.setLayoutDimensions(h: Int) {
    layoutParams = layoutParams.apply { height = h }
}
