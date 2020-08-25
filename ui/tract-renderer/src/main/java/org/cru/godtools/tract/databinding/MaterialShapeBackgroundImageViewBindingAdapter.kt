package org.cru.godtools.tract.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.shape.CornerTreatment
import org.cru.godtools.tract.widget.MaterialShapeBackgroundImageView

@BindingAdapter("cornerBottomRight", "cornerBottomLeft", requireAll = false)
internal fun MaterialShapeBackgroundImageView.setCornerTreatments(
    bottomRight: CornerTreatment?,
    bottomLeft: CornerTreatment?
) {
    backgroundShapeAppearanceModel = backgroundShapeAppearanceModel.toBuilder().apply {
        bottomRight?.let { setBottomRightCorner(bottomRight) }
        bottomLeft?.let { setBottomLeftCorner(bottomLeft) }
    }.build()
}
