package org.cru.godtools.xml.model

import android.text.TextUtils
import android.view.View
import org.cru.godtools.tool.model.stylesParent

@Deprecated("Use kotlin mpp library instead")
typealias Base = org.cru.godtools.tool.model.Base

@Deprecated("Use kotlin mpp library instead", ReplaceWith("stylesParent", "org.cru.godtools.tool.model.stylesParent"))
inline val Base?.stylesParent get() = stylesParent

val Base?.layoutDirection
    get() = this?.manifest?.locale?.let { TextUtils.getLayoutDirectionFromLocale(it) } ?: View.LAYOUT_DIRECTION_INHERIT
