package org.cru.godtools.xml.model.tips

import org.cru.godtools.tool.model.tips.textColor

@Deprecated("Use kotlin mpp library instead")
typealias Tip = org.cru.godtools.tool.model.tips.Tip

@Deprecated("Use kotlin mpp library instead", ReplaceWith("textColor", "org.cru.godtools.tool.model.tips.textColor"))
inline val Tip?.textColor get() = textColor
