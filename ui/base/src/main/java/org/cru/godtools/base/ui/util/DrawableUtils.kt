package org.cru.godtools.base.ui.util

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable?.tint(@ColorInt color: Int) =
    this?.let { DrawableCompat.wrap(it).mutate() }?.apply { DrawableCompat.setTint(this, color) }
