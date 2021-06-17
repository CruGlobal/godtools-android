package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.ImageScaleType
import org.cru.godtools.tool.model.tract.backgroundImageGravity

@ColorInt
private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X

@Deprecated("Use kotlin mpp library instead")
typealias TractPage = org.cru.godtools.tool.model.tract.TractPage

@get:ColorInt
val TractPage?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val TractPage?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundImageGravity", "org.cru.godtools.tool.model.tract.backgroundImageGravity")
)
inline val TractPage?.backgroundImageGravity get() = backgroundImageGravity
