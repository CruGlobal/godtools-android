package org.cru.godtools.xml.model

@Deprecated("Use kotlin mpp library instead")
typealias Button = org.cru.godtools.tool.model.Button

val Button?.buttonColor get() = this?.buttonColor ?: stylesParent.primaryColor
val Button?.textColor get() = this?.textColor ?: stylesParent.primaryTextColor
