package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.backgroundImageGravity
import org.cru.godtools.tool.model.categoryLabelColor
import org.cru.godtools.tool.model.lessonNavBarColor

@ColorInt
private val DEFAULT_BACKGROUND_COLOR = Color.WHITE
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL

@Deprecated("Use kotlin mpp library instead")
typealias Manifest = org.cru.godtools.tool.model.Manifest

@get:ColorInt
val Manifest?.navBarColor get() = this?.navBarColor ?: primaryColor
@get:ColorInt
val Manifest?.navBarControlColor get() = this?.navBarControlColor ?: primaryTextColor

@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("lessonNavBarColor", "org.cru.godtools.tool.model.lessonNavBarColor")
)
@get:ColorInt
inline val Manifest?.lessonNavBarColor get() = lessonNavBarColor
@get:ColorInt
val Manifest?.lessonNavBarControlColor get() = this?.navBarControlColor ?: primaryColor

@get:ColorInt
val Manifest?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundImageGravity", "org.cru.godtools.tool.model.backgroundImageGravity")
)
inline val Manifest?.backgroundImageGravity get() = backgroundImageGravity
val Manifest?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("categoryLabelColor", "org.cru.godtools.tool.model.categoryLabelColor")
)
@get:ColorInt
inline val Manifest?.categoryLabelColor get() = categoryLabelColor
