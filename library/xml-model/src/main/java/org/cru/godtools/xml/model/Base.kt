package org.cru.godtools.xml.model

import android.text.TextUtils
import android.view.View

interface Base {
    val stylesParent: Styles?

    val manifest: Manifest
}

val Base?.stylesParent get() = this?.stylesParent
val Base?.layoutDirection
    get() = this?.manifest?.locale?.let { TextUtils.getLayoutDirectionFromLocale(it) } ?: View.LAYOUT_DIRECTION_INHERIT
