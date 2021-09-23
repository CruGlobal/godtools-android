package org.cru.godtools.base.tool.databinding

import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableEndTarget
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableStartTarget
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.dagger.picasso
import org.cru.godtools.base.tool.ui.util.getTypeface
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.tool.model.Resource
import org.cru.godtools.tool.model.Text
import org.cru.godtools.tool.model.gravity
import org.cru.godtools.tool.model.textAlign
import org.cru.godtools.tool.model.textColor
import org.cru.godtools.tool.model.textScale
import splitties.bitflags.minusFlag
import splitties.bitflags.withFlag

@BindingAdapter("android:text", "android:textSize", requireAll = false)
fun TextView.bindTextNode(text: Text?, textSize: Float?) {
    this.text = text?.text
    setTypeface(text?.manifest?.getTypeface(context), text?.typefaceStyle ?: Typeface.NORMAL)
    paintFlags = paintFlags.let {
        if (Text.Style.UNDERLINE in text?.textStyles.orEmpty()) it.withFlag(Paint.UNDERLINE_TEXT_FLAG)
        else it.minusFlag(Paint.UNDERLINE_TEXT_FLAG)
    }
    val size = text.textScale * (textSize ?: context.resources.getDimension(R.dimen.tool_content_text_size_base))
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())

    setTextColor(text.textColor)

    // set the alignment for the text
    gravity = (gravity and Gravity.VERTICAL_GRAVITY_MASK) or text.textAlign.gravity
}

@BindingAdapter("android:drawableStart", "drawableStartSize")
fun TextView.bindDrawableStartResource(resource: Resource?, drawableStartSize: Int) {
    val target = TextViewDrawableStartTarget.of(this)
    val file = resource?.localName?.let { context.toolFileManager.getFileBlocking(it) }
    val imageSize = dpToPixelSize(drawableStartSize, resources)
    if (file != null) {
        context.picasso.load(file)
            .resize(imageSize, imageSize)
            .centerInside()
            .into(target)
    } else {
        context.picasso.cancelRequest(target)
        target.updateDrawable(null)
    }
}

@BindingAdapter("android:drawableEnd", "drawableEndSize")
fun TextView.bindDrawableEndResource(resource: Resource?, drawableEndSize: Int) {
    val target = TextViewDrawableEndTarget.of(this)
    val file = resource?.localName?.let { context.toolFileManager.getFileBlocking(it) }
    val imageSize = dpToPixelSize(drawableEndSize, resources)
    if (file != null) {
        context.picasso.load(file)
            .resize(imageSize, imageSize)
            .centerInside()
            .into(target)
    } else {
        context.picasso.cancelRequest(target)
        target.updateDrawable(null)
    }
}

private val Text.typefaceStyle
    get() = Typeface.NORMAL
        .withFlag(if (Text.Style.BOLD in textStyles) Typeface.BOLD else 0)
        .withFlag(if (Text.Style.ITALIC in textStyles) Typeface.ITALIC else 0)
