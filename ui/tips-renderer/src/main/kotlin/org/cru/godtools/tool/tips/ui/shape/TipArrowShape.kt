package org.cru.godtools.tool.tips.ui.shape

import android.content.Context
import org.ccci.gto.android.common.material.shape.AngledCutCornerTreatment
import org.cru.godtools.tool.tips.R

internal object TipArrowShape {
    @JvmStatic
    fun startCornerTreatment(context: Context) = AngledCutCornerTreatment(
        context.resources.getDimension(R.dimen.tool_tips_arrow_size),
        context.resources.getDimension(R.dimen.tool_tips_arrow_width) / 2f
    )

    @JvmStatic
    fun endCornerTreatment(context: Context) = AngledCutCornerTreatment(
        context.resources.getDimension(R.dimen.tool_tips_arrow_width) / 2f,
        context.resources.getDimension(R.dimen.tool_tips_arrow_size)
    )
}
