package org.cru.godtools.xml.model

import android.text.TextUtils

interface BaseModel {
    val stylesParent: Styles?

    val manifest: Manifest
    val page: Page

    val layoutDirection get() = TextUtils.getLayoutDirectionFromLocale(manifest.locale)
}

val BaseModel?.stylesParent get() = this?.stylesParent
