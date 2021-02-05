package org.cru.godtools.base.tool.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView.ScaleHelper
import org.cru.godtools.xml.model.ImageScaleType

class SimpleScaledPicassoImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SimplePicassoImageView(context, attrs, defStyleAttr), ScaledPicassoImageView {
    override val helper = ScaleHelper(this, attrs, defStyleAttr, defStyleRes)

    override fun setScaleType(type: ImageScaleType) {
        helper.scaleType = type
    }

    override fun setGravityHorizontal(gravity: GravityHorizontal) {
        helper.gravityHorizontal = gravity
    }

    override fun setGravityVertical(gravity: GravityVertical) {
        helper.gravityVertical = gravity
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        helper.onSetImageDrawable()
    }
}
