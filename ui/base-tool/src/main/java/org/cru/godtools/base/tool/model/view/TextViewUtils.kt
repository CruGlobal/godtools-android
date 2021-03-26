package org.cru.godtools.base.tool.model.view

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.defaultTextColor
import org.cru.godtools.xml.model.textAlign
import org.cru.godtools.xml.model.textScale
import org.cru.godtools.xml.model.textSize

internal fun Text?.bindTo(view: TextView, textSize: Float? = null, @ColorInt defaultTextColor: Int? = null) {
    view.text = this?.text
    view.typeface = this?.getTypeface(view.context)
    val size = textScale * (textSize ?: view.context.resources.getDimension(this.textSize))
    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())

    val defColor = defaultTextColor ?: this.defaultTextColor
    if (this != null) {
        view.setTextColor(getTextColor(defColor))
    } else {
        view.setTextColor(defColor)
    }

    // set the alignment for the text
    view.gravity = (view.gravity and Gravity.VERTICAL_GRAVITY_MASK) or textAlign.gravity
}

private fun Text.getTypeface(context: Context) = manifest.getTypeface(context)
