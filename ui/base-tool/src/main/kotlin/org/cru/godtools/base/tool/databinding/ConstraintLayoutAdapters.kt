package org.cru.godtools.base.tool.databinding

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
import androidx.databinding.BindingAdapter

@BindingAdapter("layout_constraintWidth_percent")
internal fun View.setConstraintLayoutWidthPercentage(width: Float) {
    val params = layoutParams
    if (
        params is ConstraintLayout.LayoutParams &&
        (params.matchConstraintPercentWidth != width || params.matchConstraintDefaultWidth != MATCH_CONSTRAINT_PERCENT)
    ) {
        params.matchConstraintPercentWidth = width
        params.matchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
        layoutParams = params
    }
}
