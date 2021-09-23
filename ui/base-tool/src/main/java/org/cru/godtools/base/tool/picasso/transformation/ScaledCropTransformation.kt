package org.cru.godtools.base.tool.picasso.transformation

import android.graphics.Bitmap
import com.squareup.picasso.Transformation
import jp.wasabeef.picasso.transformations.CropTransformation
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.cru.godtools.tool.model.ImageScaleType

data class ScaledCropTransformation(
    private val aspectRatio: Float,
    private val scaleType: ImageScaleType,
    private val gravityHorizontal: GravityHorizontal,
    private val gravityVertical: GravityVertical
) : Transformation {
    constructor(
        width: Int,
        height: Int,
        scaleType: ImageScaleType,
        gravityHorizontal: GravityHorizontal,
        gravityVertical: GravityVertical
    ) : this(if (height != 0) width.toFloat() / height.toFloat() else 1f, scaleType, gravityHorizontal, gravityVertical)

    override fun key() = toString()
    override fun transform(source: Bitmap): Bitmap = CropTransformation(
        if (scaleType == ImageScaleType.FILL_X) source.width else 0,
        if (scaleType == ImageScaleType.FILL_Y) source.height else 0,
        aspectRatio, gravityHorizontal, gravityVertical
    ).transform(source)
}
