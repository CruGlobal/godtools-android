package org.cru.godtools.base.tool.databinding

import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import org.cru.godtools.base.ui.util.tint

@BindingAdapter("navigationIconTint")
internal fun Toolbar.setNavigationIconTint(@ColorInt color: Int) {
    navigationIcon = navigationIcon.tint(color)
}

@BindingAdapter("overflowIconTint")
internal fun Toolbar.setOverflowIconTint(@ColorInt color: Int) {
    overflowIcon = overflowIcon.tint(color)
}
