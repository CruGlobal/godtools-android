package org.cru.godtools.tract.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.shape.CornerTreatment
import org.cru.godtools.tract.widget.MaterialShapeBackgroundImageView

@BindingAdapter("cornerTopLeft", "cornerTopRight", "cornerBottomRight", "cornerBottomLeft", requireAll = false)
internal fun MaterialShapeBackgroundImageView.setCornerTreatments(
    topLeft: CornerTreatment?,
    topRight: CornerTreatment?,
    bottomRight: CornerTreatment?,
    bottomLeft: CornerTreatment?
) {
    backgroundShapeAppearanceModel = backgroundShapeAppearanceModel.toBuilder().apply {
        topLeft?.let { setTopLeftCorner(topLeft) }
        topRight?.let { setTopRightCorner(topRight) }
        bottomRight?.let { setBottomRightCorner(bottomRight) }
        bottomLeft?.let { setBottomLeftCorner(bottomLeft) }
    }.build()
}
