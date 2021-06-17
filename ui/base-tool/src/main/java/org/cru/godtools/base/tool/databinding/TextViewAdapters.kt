package org.cru.godtools.base.tool.databinding

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
fun TextView.bindTextNode(text: Text?, textSize: Float?, @ColorInt defaultTextColor: Int?) {
    this.text = text?.text
    setTypeface(text?.manifest?.getTypeface(context), text?.typefaceStyle ?: Typeface.NORMAL)
    paintFlags = paintFlags.let {
        if (Text.Style.UNDERLINE in text?.textStyles.orEmpty()) it.withFlag(Paint.UNDERLINE_TEXT_FLAG)
        else it.minusFlag(Paint.UNDERLINE_TEXT_FLAG)
    }
    val size = text.textScale * (textSize ?: context.resources.getDimension(text.textSize))
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())

    val defColor = defaultTextColor ?: text.defaultTextColor
    if (text != null) {
        setTextColor(text.getTextColor(defColor))
    } else {
        setTextColor(defColor)
    }

    // set the alignment for the text
    gravity = (gravity and Gravity.VERTICAL_GRAVITY_MASK) or text.textAlign.gravity
}

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

private val Text.typefaceStyle
    get() = Typeface.NORMAL
        .withFlag(if (Text.Style.BOLD in textStyles) Typeface.BOLD else 0)
        .withFlag(if (Text.Style.ITALIC in textStyles) Typeface.ITALIC else 0)
