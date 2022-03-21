package org.cru.godtools.base.tool.databinding.adapters

import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.google.android.material.shape.MaterialShapeDrawable

@BindingAdapter("backgroundMaterialShapeColor")
internal fun View.materialShapeBackgroundColor(@ColorInt color: Int?) {
    backgroundMaterialShapeDrawable.fillColor = color?.let { ColorStateList.valueOf(it) }
}

@BindingAdapter("backgroundMaterialShapeCornerSize")
internal fun View.materialShapeBackgroundCornerSize(size: Float) {
    backgroundMaterialShapeDrawable.setCornerSize(size)
}

private val View.backgroundMaterialShapeDrawable
    get() = background as? MaterialShapeDrawable ?: MaterialShapeDrawable().also { background = it }
