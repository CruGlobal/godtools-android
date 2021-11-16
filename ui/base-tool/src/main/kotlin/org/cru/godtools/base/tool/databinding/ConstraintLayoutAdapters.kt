package org.cru.godtools.base.tool.databinding

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
import androidx.databinding.BindingAdapter

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
        lp.validate()
        layoutParams = lp
    }
}
