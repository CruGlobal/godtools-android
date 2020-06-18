package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import androidx.annotation.DimenRes

interface Styles : BaseModel {
    @get:ColorInt
    val primaryColor: Int get() = stylesParent.primaryColor

    @get:ColorInt
    val primaryTextColor: Int get() = stylesParent.primaryTextColor

    @get:ColorInt
    val textColor: Int get() = stylesParent.textColor

    @get:ColorInt
    val buttonColor: Int get() = stylesParent.buttonColor

    @get:DimenRes
    val textSize: Int get() = stylesParent.textSize

    val textAlign: Text.Align get() = stylesParent.textAlign
}

@get:ColorInt
val Styles?.primaryColor get() = this?.primaryColor ?: Manifest.DEFAULT_PRIMARY_COLOR
@get:ColorInt
val Styles?.primaryTextColor get() = this?.primaryTextColor ?: Manifest.DEFAULT_PRIMARY_TEXT_COLOR
@get:ColorInt
val Styles?.buttonColor get() = this?.buttonColor ?: primaryColor
@get:ColorInt
val Styles?.textColor get() = this?.textColor ?: Manifest.DEFAULT_TEXT_COLOR
@get:DimenRes
val Styles?.textSize get() = this?.textSize ?: Manifest.DEFAULT_TEXT_SIZE
val Styles?.textAlign get() = this?.textAlign ?: Manifest.DEFAULT_TEXT_ALIGN
