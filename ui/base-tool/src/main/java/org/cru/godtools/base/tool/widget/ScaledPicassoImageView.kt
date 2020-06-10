package org.cru.godtools.base.tool.widget

import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.squareup.picasso.RequestCreator
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.base.model.Dimension
import org.ccci.gto.android.common.picasso.transformation.ScaleTransformation
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.picasso.transformation.ScaledCropTransformation
import org.cru.godtools.xml.model.ImageScaleType

interface ScaledPicassoImageView : PicassoImageView {
    class ScaleHelper(view: ImageView, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) :
        PicassoImageView.Helper(view, attrs, defStyleAttr, defStyleRes) {

        var scaleType = ImageScaleType.FIT
            set(value) {
                val changed = value != field
                field = value
                if (changed) triggerUpdate()
            }
        var gravityHorizontal = GravityHorizontal.CENTER
            set(value) {
                val changed = value != field
                field = value
                if (changed) triggerUpdate()
            }
        var gravityVertical = GravityVertical.CENTER
            set(value) {
                val changed = value != field
                field = value
                if (changed) triggerUpdate()
            }

        init {
            view.context.withStyledAttributes(attrs, R.styleable.ScaledPicassoImageView, defStyleAttr, defStyleRes) {
                scaleType = ImageScaleType.values().getOrNull(getInt(R.styleable.ScaledPicassoImageView_scaleType, -1))
                    ?: scaleType
            }
        }

        override fun onSetUpdateScale(update: RequestCreator, size: Dimension) {
            when (scaleType) {
                ImageScaleType.FILL, ImageScaleType.FILL_X, ImageScaleType.FILL_Y -> {
                    if (size.width > 0 && scaleType == ImageScaleType.FILL_X) {
                        update.resize(size.width, 0).onlyScaleDown()
                    } else if (size.height > 0 && scaleType == ImageScaleType.FILL_Y) {
                        update.resize(0, size.height).onlyScaleDown()
                    } else {
                        update.transform(ScaleTransformation(size.width, size.height, true))
                    }

                    // crop with gravity
                    update.transform(
                        ScaledCropTransformation(
                            size.width, size.height, scaleType, gravityHorizontal, gravityVertical
                        )
                    )
                }
                ImageScaleType.FIT -> {
                    update.resize(size.width, size.height).onlyScaleDown()
                    update.centerInside()
                }
            }
        }
    }

    fun setScaleType(type: ImageScaleType)
    fun setGravityHorizontal(gravity: GravityHorizontal)
    fun setGravityVertical(gravity: GravityVertical)
}
