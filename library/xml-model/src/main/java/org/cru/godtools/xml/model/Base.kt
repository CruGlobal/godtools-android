package org.cru.godtools.xml.model

import android.text.TextUtils
import android.view.View

interface Base {
    val stylesParent: Styles?

    val manifest: Manifest

    val layoutDirection get() = TextUtils.getLayoutDirectionFromLocale(manifest.locale)
}

val Base?.stylesParent get() = this?.stylesParent
val Base?.layoutDirection get() = this?.layoutDirection ?: View.LAYOUT_DIRECTION_INHERIT
