package org.cru.godtools.base.tool.model.view

import android.content.Context
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.graphics.Typeface.NORMAL
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.defaultTextColor
import org.cru.godtools.xml.model.textAlign
import org.cru.godtools.xml.model.textScale
import org.cru.godtools.xml.model.textSize
import splitties.bitflags.minusFlag
import splitties.bitflags.withFlag

internal fun Text?.bindTo(view: TextView, textSize: Float? = null, @ColorInt defaultTextColor: Int? = null) {
    view.text = this?.text
    view.setTypeface(this?.getTypeface(view.context), this?.typefaceStyle ?: NORMAL)
    view.paintFlags = view.paintFlags.let {
        if (Text.Style.UNDERLINE in this?.textStyles.orEmpty()) it.withFlag(UNDERLINE_TEXT_FLAG)
        else it.minusFlag(UNDERLINE_TEXT_FLAG)
    }
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
private val Text.typefaceStyle get() = NORMAL
    .withFlag(if (Text.Style.BOLD in textStyles) BOLD else 0)
    .withFlag(if (Text.Style.ITALIC in textStyles) ITALIC else 0)
