package org.cru.godtools.xml.model.tract

import androidx.annotation.ColorInt
import org.cru.godtools.xml.model.primaryColor
import org.cru.godtools.xml.model.stylesParent

@Deprecated("Use kotlin mpp library instead")
typealias CallToAction = org.cru.godtools.tool.model.tract.CallToAction

@get:ColorInt
val CallToAction?.controlColor: Int get() = this?.controlColor ?: stylesParent.primaryColor
