package org.cru.godtools.xml.model

import org.cru.godtools.tool.model.type

@Deprecated("Use kotlin mpp library instead")
typealias Input = org.cru.godtools.tool.model.Input

@Deprecated("Use kotlin mpp library instead", ReplaceWith("type", "org.cru.godtools.tool.model.type"))
inline val Input?.type get() = type
