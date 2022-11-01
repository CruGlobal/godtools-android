package org.cru.godtools.base.tool.ui.util

import android.text.TextUtils
import android.view.View
import org.cru.godtools.shared.tool.parser.model.Base
import org.cru.godtools.shared.tool.parser.model.manifest

val Base?.layoutDirection
    get() = manifest?.locale?.let { TextUtils.getLayoutDirectionFromLocale(it) } ?: View.LAYOUT_DIRECTION_INHERIT
