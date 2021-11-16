package org.cru.godtools.base.tool.databinding

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.tool.model.Dimension
import org.cru.godtools.tool.model.Dimension.Percent
import org.cru.godtools.tool.model.Dimension.Pixels

@BindingAdapter("android:layout_width")
internal fun View.setLayoutWidth(w: Dimension?) {
    when (w) {
        is Pixels -> {
            val size = dpToPixelSize(w.value, resources)
            val lp = layoutParams
            if (size != lp.width) {
                lp.width = size
                layoutParams = lp
            }
        }
        is Percent -> when (val lp = layoutParams) {
            is ConstraintLayout.LayoutParams -> setConstraintLayoutWidthPercentage(w.value)
            else -> TODO("Unsupported LayoutParams for Percent Dimension: $lp")
        }
        null -> when (val lp = layoutParams) {
            is ConstraintLayout.LayoutParams -> {
                if (
                    lp.width != MATCH_PARENT ||
                    lp.matchConstraintDefaultWidth != MATCH_CONSTRAINT_SPREAD ||
                    lp.matchConstraintPercentWidth != 1f
                ) {
                    lp.width = MATCH_PARENT
                    lp.matchConstraintDefaultWidth = MATCH_CONSTRAINT_SPREAD
                    lp.matchConstraintPercentWidth = 1f
                    layoutParams = lp
                }
            }
            else -> TODO("Unsupported LayoutParams for null width: $lp")
        }
        else -> TODO("Unsupported width: $w")
    }
}
