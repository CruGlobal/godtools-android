package org.cru.godtools.base.tool.databinding.converters

import android.content.res.ColorStateList
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.BindingConversion
import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.extensions.android.colorint.toColorInt

@BindingConversion
internal fun convertColorToColorInt(color: Color) = color.toColorInt()

@BindingConversion
internal fun convertColorToColorStateList(color: Color) = ColorStateList.valueOf(color.toColorInt())

@BindingConversion
internal fun convertColorToDrawable(color: Color) = color.toColorInt().toDrawable()
