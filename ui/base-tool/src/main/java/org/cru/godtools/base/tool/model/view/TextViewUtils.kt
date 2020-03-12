@file:JvmName("TextViewUtils")

package org.cru.godtools.base.tool.model.view

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import org.cru.godtools.xml.model.Text

@JvmOverloads
@JvmName("bind")
fun Text?.bindTo(view: TextView?, @DimenRes textSize: Int? = null, @ColorInt defaultTextColor: Int? = null) {
    if (view == null) return

    view.text = Text.getText(this)
    view.typeface = this?.getTypeface(view.context)
    val size = view.context.resources.getDimension(textSize ?: Text.textSize(this))
    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (size * Text.getTextScale(this)).toFloat())

    val defColor = defaultTextColor ?: Text.defaultTextColor(this)
    if (this != null) {
        view.setTextColor(getTextColor(defColor))
    } else {
        view.setTextColor(defColor)
    }

    // set the alignment for the text
    view.gravity = (view.gravity and Gravity.VERTICAL_GRAVITY_MASK) or Text.getTextAlign(this).mGravity
}

private fun Text.getTypeface(context: Context) = ManifestViewUtils.getTypeface(manifest, context)
