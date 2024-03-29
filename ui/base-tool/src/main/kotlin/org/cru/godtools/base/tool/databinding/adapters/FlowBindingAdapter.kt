package org.cru.godtools.base.tool.databinding.adapters

import androidx.constraintlayout.helper.widget.Flow
import androidx.databinding.BindingAdapter
import org.cru.godtools.shared.tool.parser.model.Gravity.Horizontal

@BindingAdapter("flow_horizontalBias")
internal fun Flow.setHorizontalBias(gravity: Horizontal) = setHorizontalBias(
    when (gravity) {
        Horizontal.START -> 0f
        Horizontal.CENTER -> 0.5f
        Horizontal.END -> 1f
    }
)
