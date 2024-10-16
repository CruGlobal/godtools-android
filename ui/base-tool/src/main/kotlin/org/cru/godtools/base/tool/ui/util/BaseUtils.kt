package org.cru.godtools.base.tool.ui.util

import android.text.TextUtils
import android.view.View
import org.cru.godtools.shared.tool.parser.model.Base

val Base?.layoutDirection
    get() = this?.manifest?.locale?.let { TextUtils.getLayoutDirectionFromLocale(it) } ?: View.LAYOUT_DIRECTION_INHERIT
