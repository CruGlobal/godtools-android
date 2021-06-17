package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.textAlign

@Deprecated("Use kotlin mpp library instead")
typealias Text = org.cru.godtools.tool.model.Text

@get:ColorInt
val Text?.defaultTextColor get() = stylesParent.textColor

@Deprecated("Use kotlin mpp library instead", ReplaceWith("textAlign", "org.cru.godtools.tool.model.textAlign"))
inline val Text?.textAlign get() = textAlign
@get:ColorInt
val Text?.textColor get() = this?.textColor ?: stylesParent.textColor
val Text?.textScale get() = this?.textScale ?: DEFAULT_TEXT_SCALE
