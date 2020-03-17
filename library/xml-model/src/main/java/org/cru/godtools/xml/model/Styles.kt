package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import androidx.annotation.DimenRes

interface Styles : BaseModel {
    @JvmDefault
    @get:ColorInt
    val primaryColor: Int get() = stylesParent.primaryColor

    @JvmDefault
    @get:ColorInt
    val primaryTextColor: Int get() = stylesParent.primaryTextColor

    @JvmDefault
    @get:ColorInt
    val textColor: Int get() = stylesParent.textColor

    @JvmDefault
    @get:ColorInt
    val buttonColor: Int get() = stylesParent.buttonColor

    @JvmDefault
    @get:DimenRes
    val textSize: Int get() = stylesParent.textSize

    @JvmDefault
    val textAlign: Text.Align get() = stylesParent.textAlign
}

@get:ColorInt
val Styles?.primaryColor get() = this?.primaryColor ?: Manifest.getDefaultPrimaryColor()
@get:ColorInt
val Styles?.primaryTextColor get() = this?.primaryTextColor ?: Manifest.getDefaultPrimaryTextColor()
@get:ColorInt
val Styles?.buttonColor get() = this?.buttonColor ?: primaryColor
@get:ColorInt
val Styles?.textColor get() = this?.textColor ?: Manifest.getDefaultTextColor()
@get:DimenRes
val Styles?.textSize get() = this?.textSize ?: Manifest.getDefaultTextSize()
val Styles?.textAlign get() = this?.textAlign ?: Manifest.getDefaultTextAlign()
