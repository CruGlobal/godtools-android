@file:JvmName("TextViewUtils")

package org.cru.godtools.base.tool.model.view

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.textAlign
import org.cru.godtools.xml.model.textScale
import org.cru.godtools.xml.model.textSize

@JvmOverloads
@JvmName("bind")
fun Text?.bindTo(view: TextView?, @DimenRes textSize: Int? = null, @ColorInt defaultTextColor: Int? = null) {
    if (view == null) return
    bindTo(view, textSize?.let { view.context.resources.getDimension(it) }, defaultTextColor)
}

internal fun Text?.bindTo(view: TextView, textSize: Float? = null, @ColorInt defaultTextColor: Int? = null) {
    view.text = this?.text
    view.typeface = this?.getTypeface(view.context)
    val size = textScale * (textSize ?: view.context.resources.getDimension(this.textSize))
    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())

    val defColor = defaultTextColor ?: Text.defaultTextColor(this)
    if (this != null) {
        view.setTextColor(getTextColor(defColor))
    } else {
        view.setTextColor(defColor)
    }

    // set the alignment for the text
    view.gravity = (view.gravity and Gravity.VERTICAL_GRAVITY_MASK) or textAlign.gravity
}

private fun Text.getTypeface(context: Context) = ManifestViewUtils.getTypeface(manifest, context)
