package org.cru.godtools.xml.model.tract

import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.ImageScaleType
import org.cru.godtools.tool.model.tract.backgroundColor
import org.cru.godtools.tool.model.tract.backgroundImageGravity

private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X

@Deprecated("Use kotlin mpp library instead")
typealias Card = org.cru.godtools.tool.model.tract.Card

@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundColor", "org.cru.godtools.tool.model.tract.backgroundColor")
)
@get:ColorInt
inline val Card?.backgroundColor get() = backgroundColor
@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundImageGravity", "org.cru.godtools.tool.model.tract.backgroundImageGravity")
)
inline val Card?.backgroundImageGravity get() = backgroundImageGravity
val Card?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
