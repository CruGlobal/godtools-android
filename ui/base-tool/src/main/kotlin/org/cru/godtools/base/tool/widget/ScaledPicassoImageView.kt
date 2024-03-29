package org.cru.godtools.base.tool.widget

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.base.model.Dimension
import org.ccci.gto.android.common.picasso.transformation.ScaleTransformation
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.tool.picasso.transformation.ScaledCropTransformation
import org.cru.godtools.shared.tool.parser.model.ImageScaleType
import org.cru.godtools.tool.R

interface ScaledPicassoImageView : PicassoImageView {
    class ScaleHelper(
        view: ImageView,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int,
        picasso: Picasso
    ) : PicassoImageView.Helper(view, attrs, defStyleAttr, defStyleRes, picasso) {

        var scaleType: ImageScaleType? = null
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
            }
        }

        override fun onSetUpdateScale(update: RequestCreator, size: Dimension) {
            when (val scaleType = scaleType) {
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
                        ScaledCropTransformation(size.width, size.height, scaleType, gravityHorizontal, gravityVertical)
                    )
                }
                ImageScaleType.FIT -> {
                    update.resize(size.width, size.height).onlyScaleDown()
                    update.centerInside()
                }
                else -> super.onSetUpdateScale(update, size)
            }
        }

        fun onSetImageDrawable() {
            updateImageMatrix()
        }

        private fun updateImageMatrix() {
            if (imageView.scaleType != ImageView.ScaleType.MATRIX) return
            val scaleType = scaleType ?: return
            val drawable = imageView.drawable ?: return

            val dwidth = drawable.intrinsicWidth.toFloat()
            val dheight = drawable.intrinsicHeight.toFloat()
            val vwidth = imageView.run { width - paddingLeft - paddingRight }.toFloat()
            val vheight = imageView.run { height - paddingTop - paddingBottom }.toFloat()

            imageView.imageMatrix = Matrix().apply {
                val widthScale = vwidth / dwidth
                val heightScale = vheight / dheight
                val scale = when (scaleType) {
                    ImageScaleType.FILL_X -> widthScale
                    ImageScaleType.FILL_Y -> heightScale
                    ImageScaleType.FILL -> if (widthScale > heightScale) widthScale else heightScale
                    ImageScaleType.FIT -> if (widthScale > heightScale) heightScale else widthScale
                }
                setScale(scale, scale)

                postTranslate(
                    when (gravityHorizontal) {
                        GravityHorizontal.LEFT -> 0f
                        GravityHorizontal.CENTER -> (vwidth - (dwidth * scale)) / 2
                        GravityHorizontal.RIGHT -> vwidth - (dwidth * scale)
                    },
                    when (gravityVertical) {
                        GravityVertical.TOP -> 0f
                        GravityVertical.CENTER -> (vheight - (dheight * scale)) / 2
                        GravityVertical.BOTTOM -> vheight - (dheight * scale)
                    }
                )
            }
        }
    }

    fun setImageDrawable(drawable: Drawable?)
    var scaleType: ImageScaleType?
    fun setGravityHorizontal(gravity: GravityHorizontal)
    fun setGravityVertical(gravity: GravityVertical)
}
