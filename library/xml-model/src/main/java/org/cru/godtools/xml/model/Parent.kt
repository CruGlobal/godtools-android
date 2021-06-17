package org.cru.godtools.xml.model

@Deprecated("Use kotlin mpp library instead")
typealias Parent = org.cru.godtools.tool.model.Parent

internal inline val Parent.contentTips get() = content.flatMap { it.tips }
