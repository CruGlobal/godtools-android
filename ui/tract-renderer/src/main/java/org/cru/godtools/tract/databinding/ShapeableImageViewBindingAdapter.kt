package org.cru.godtools.tract.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerTreatment

@BindingAdapter("cornerTopLeft", "cornerTopRight", "cornerBottomRight", "cornerBottomLeft", requireAll = false)
internal fun ShapeableImageView.setCornerTreatments(
    topLeft: CornerTreatment?,
    topRight: CornerTreatment?,
    bottomRight: CornerTreatment?,
    bottomLeft: CornerTreatment?
) {
    shapeAppearanceModel = shapeAppearanceModel.toBuilder().apply {
        topLeft?.let { setTopLeftCorner(topLeft) }
        topRight?.let { setTopRightCorner(topRight) }
        bottomRight?.let { setBottomRightCorner(bottomRight) }
        bottomLeft?.let { setBottomLeftCorner(bottomLeft) }
    }.build()
}
