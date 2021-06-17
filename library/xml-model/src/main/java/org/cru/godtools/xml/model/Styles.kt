package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.buttonStyle
import org.cru.godtools.tool.model.primaryColor
import org.cru.godtools.tool.model.primaryTextColor
import org.cru.godtools.tool.model.textAlign
import org.cru.godtools.tool.model.textColor

internal const val DEFAULT_TEXT_SCALE = 1.0

@Deprecated("Use kotlin mpp library instead")
typealias Styles = org.cru.godtools.tool.model.Styles

@Deprecated("Use kotlin mpp library instead", ReplaceWith("primaryColor", "org.cru.godtools.tool.model.primaryColor"))
@get:ColorInt
inline val Styles?.primaryColor get() = primaryColor
@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("primaryTextColor", "org.cru.godtools.tool.model.primaryTextColor")
)
@get:ColorInt
inline val Styles?.primaryTextColor get() = primaryTextColor
@Deprecated("Use kotlin mpp library instead", ReplaceWith("textColor", "org.cru.godtools.tool.model.textColor"))
@get:ColorInt
inline val Styles?.textColor get() = textColor
val Styles?.textScale get() = this?.textScale ?: DEFAULT_TEXT_SCALE
@Deprecated("Use kotlin mpp library instead", ReplaceWith("textAlign", "org.cru.godtools.tool.model.textAlign"))
inline val Styles?.textAlign get() = textAlign

@Deprecated("Use kotlin mpp library instead", ReplaceWith("buttonStyle", "org.cru.godtools.tool.model.buttonStyle"))
inline val Styles?.buttonStyle get() = buttonStyle
