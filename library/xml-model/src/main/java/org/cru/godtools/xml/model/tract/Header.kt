package org.cru.godtools.xml.model.tract

import org.cru.godtools.tool.model.tract.backgroundColor

@Deprecated("Use kotlin mpp library instead")
typealias Header = org.cru.godtools.tool.model.tract.Header

@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundColor", "org.cru.godtools.tool.model.tract.backgroundColor")
)
inline val Header?.backgroundColor get() = backgroundColor
