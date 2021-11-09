package org.cru.godtools.base.tool.ui.util

import android.text.TextUtils
import android.view.View
import org.cru.godtools.tool.model.Base
import org.cru.godtools.tool.model.manifest

val Base?.layoutDirection
    get() = manifest?.locale?.let { TextUtils.getLayoutDirectionFromLocale(it) } ?: View.LAYOUT_DIRECTION_INHERIT
