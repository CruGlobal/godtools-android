package org.cru.godtools.tract.ui.tips

import android.content.Context
import org.ccci.gto.android.common.material.shape.AngledCutCornerTreatment
import org.cru.godtools.tract.R

internal object TipArrowShape {
    @JvmStatic
    fun startCornerTreatment(context: Context) = AngledCutCornerTreatment(
        context.resources.getDimension(R.dimen.tract_tips_arrow_size),
        context.resources.getDimension(R.dimen.tract_tips_arrow_width) / 2f
    )

    @JvmStatic
    fun endCornerTreatment(context: Context) = AngledCutCornerTreatment(
        context.resources.getDimension(R.dimen.tract_tips_arrow_width) / 2f,
        context.resources.getDimension(R.dimen.tract_tips_arrow_size)
    )
}
