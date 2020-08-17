package org.cru.godtools.tract.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.cru.godtools.tract.R

class MaterialShapeBackgroundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val backgroundShapeDrawable =
        MaterialShapeDrawable(ShapeAppearanceModel.builder(context, attrs, defStyleAttr, 0).build())
            .also { it.initializeElevationOverlay(context) }

    init {
        background = backgroundShapeDrawable

        context.withStyledAttributes(attrs, R.styleable.MaterialShapeBackgroundImageView, defStyleAttr) {
            elevation = getDimension(R.styleable.MaterialShapeBackgroundImageView_elevation, 0f)
            backgroundFillColor = getColorStateList(R.styleable.MaterialShapeBackgroundImageView_backgroundFillColor)
        }
    }

    var backgroundFillColor
        get() = backgroundShapeDrawable.fillColor
        set(value) {
            backgroundShapeDrawable.fillColor = value
        }
    var backgroundShapeAppearanceModel
        get() = backgroundShapeDrawable.shapeAppearanceModel
        set(value) {
            backgroundShapeDrawable.shapeAppearanceModel = value
        }

    override fun setElevation(elevation: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) super.setElevation(elevation)
        backgroundShapeDrawable.elevation = elevation
    }
}
