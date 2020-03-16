package org.cru.godtools.xml.model

internal interface BaseModel {
    val stylesParent: Styles?
}

internal val BaseModel?.stylesParent get() = this?.stylesParent
