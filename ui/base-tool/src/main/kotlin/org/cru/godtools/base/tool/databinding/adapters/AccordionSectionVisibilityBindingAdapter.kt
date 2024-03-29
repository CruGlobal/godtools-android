package org.cru.godtools.base.tool.databinding.adapters

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.ccci.gto.android.common.androidx.databinding.adapters.visibleIf
import org.ccci.gto.android.common.animation.HeightAndAlphaVisibilityAnimator

private const val VISIBLE_IF = "accordionSectionVisibleIf"

@BindingAdapter(VISIBLE_IF)
internal fun View.animatedVisibleIf(visible: Boolean) {
    val animator = HeightAndAlphaVisibilityAnimator.of(this, isVisible = visible) {
        interpolator = FastOutSlowInInterpolator()
        visibleIf(visible)
    }
    if (visible) {
        animator.show()
    } else {
        animator.hide()
    }
}
