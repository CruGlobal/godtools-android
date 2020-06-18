package org.cru.godtools.xml.model

interface BaseModel {
    val stylesParent: Styles?

    val manifest: Manifest
    val page: Page
}

val BaseModel?.stylesParent get() = this?.stylesParent
