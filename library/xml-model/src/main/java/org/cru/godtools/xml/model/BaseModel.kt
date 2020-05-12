package org.cru.godtools.xml.model

interface BaseModel {
    val stylesParent: Styles?
}

val BaseModel?.stylesParent get() = this?.stylesParent
