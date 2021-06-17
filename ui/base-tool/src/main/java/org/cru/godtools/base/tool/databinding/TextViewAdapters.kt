package org.cru.godtools.base.tool.databinding

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableEndTarget
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableStartTarget
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.model.view.getTypeface
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.xml.model.Resource
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.defaultTextColor
import org.cru.godtools.xml.model.textAlign
import org.cru.godtools.xml.model.textScale
import org.cru.godtools.xml.model.textSize
import splitties.bitflags.minusFlag
import splitties.bitflags.withFlag

@BindingAdapter("android:text", "android:textSize", "defaultTextColor", requireAll = false)
fun TextView.bindTextNode(text: Text?, textSize: Float?, @ColorInt defaultTextColor: Int?) =
    text.bindTo(this, textSize, defaultTextColor)

@BindingAdapter("android:drawableStart", "drawableStartSize")
fun TextView.bindDrawableStartResource(resource: Resource?, drawableStartSize: Int) {
    val target = TextViewDrawableStartTarget.of(this)
    val file = resource?.localName?.let { context.toolFileManager.getFileBlocking(it) }
    val imageSize = dpToPixelSize(drawableStartSize, resources)
    if (file != null) {
        // TODO: figure out how to provide Picasso Singleton from Dagger
        Picasso.get().load(file)
            .resize(imageSize, imageSize)
            .centerInside()
            .into(target)
    } else {
        Picasso.get().cancelRequest(target)
        target.updateDrawable(null)
    }
}

@BindingAdapter("android:drawableEnd", "drawableEndSize")
fun TextView.bindDrawableEndResource(resource: Resource?, drawableEndSize: Int) {
    val target = TextViewDrawableEndTarget.of(this)
    val file = resource?.localName?.let { context.toolFileManager.getFileBlocking(it) }
    val imageSize = dpToPixelSize(drawableEndSize, resources)
    if (file != null) {
        // TODO: figure out how to provide Picasso Singleton from Dagger
        Picasso.get().load(file)
            .resize(imageSize, imageSize)
            .centerInside()
            .into(target)
    } else {
        Picasso.get().cancelRequest(target)
        target.updateDrawable(null)
    }
}

internal fun Text?.bindTo(view: TextView, textSize: Float? = null, @ColorInt defaultTextColor: Int? = null) {
    view.text = this?.text
    view.setTypeface(this?.getTypeface(view.context), this?.typefaceStyle ?: Typeface.NORMAL)
    view.paintFlags = view.paintFlags.let {
        if (Text.Style.UNDERLINE in this?.textStyles.orEmpty()) it.withFlag(Paint.UNDERLINE_TEXT_FLAG)
        else it.minusFlag(Paint.UNDERLINE_TEXT_FLAG)
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
private val Text.typefaceStyle
    get() = Typeface.NORMAL
        .withFlag(if (Text.Style.BOLD in textStyles) Typeface.BOLD else 0)
        .withFlag(if (Text.Style.ITALIC in textStyles) Typeface.ITALIC else 0)
