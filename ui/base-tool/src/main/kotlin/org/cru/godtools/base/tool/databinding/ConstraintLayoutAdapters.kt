package org.cru.godtools.base.tool.databinding

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
import androidx.databinding.BindingAdapter
import org.cru.godtools.tool.model.Gravity

@BindingAdapter("layout_constraintWidth_percent")
internal fun View.setConstraintLayoutWidthPercentage(width: Float) {
    val lp = layoutParams as? ConstraintLayout.LayoutParams ?: return
    if (
        lp.matchConstraintPercentWidth != width ||
        lp.matchConstraintDefaultWidth != MATCH_CONSTRAINT_PERCENT ||
        lp.width != MATCH_CONSTRAINT
    ) {
        lp.width = MATCH_CONSTRAINT
        lp.matchConstraintPercentWidth = width
        lp.matchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
        layoutParams = lp
    }
}

@BindingAdapter("layout_constraintHorizontal_bias")
internal fun View.setConstraintLayoutHorizontalBias(gravity: Gravity.Horizontal?) {
    val lp = layoutParams as? ConstraintLayout.LayoutParams ?: return
    val bias = when (gravity) {
        Gravity.Horizontal.START -> 0f
        Gravity.Horizontal.CENTER -> 0.5f
        Gravity.Horizontal.END -> 1f
        else -> 0.5f
    }

    if (lp.horizontalBias != bias) {
        lp.horizontalBias = bias
        layoutParams = lp
    }
}
