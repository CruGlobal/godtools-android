package org.cru.godtools.base.tool.databinding

import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableEndTarget
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableStartTarget
import org.cru.godtools.base.fileManager
import org.cru.godtools.base.tool.model.view.bindTo
import org.cru.godtools.xml.model.Resource
import org.cru.godtools.xml.model.Text

@BindingAdapter("android:text", "android:textSize", "defaultTextColor", requireAll = false)
fun TextView.bindTextNode(text: Text?, textSize: Float?, @ColorInt defaultTextColor: Int?) =
    text.bindTo(this, textSize, defaultTextColor)

@BindingAdapter("android:drawableStart", "startImageSize")
fun TextView.bindDrawableStartResource(resource: Resource?, startImageSize: Int) {
    val target = TextViewDrawableStartTarget.of(this)
    val file = resource?.localName?.let { context.fileManager.getFileBlocking(it) }
    val imageSize = (startImageSize * resources.displayMetrics.density).toInt()
    if (file != null) {
        Picasso.get().load(file)
            .resize(imageSize, imageSize)
            .into(target)
    } else {
        Picasso.get().cancelRequest(target)
        target.updateDrawable(null)
    }
}

@BindingAdapter("android:drawableEnd", "endImageSize")
fun TextView.bindDrawableEndResource(resource: Resource?, endImageSize: Int) {
    val target = TextViewDrawableEndTarget.of(this)
    val file = resource?.localName?.let { context.fileManager.getFileBlocking(it) }
    val imageSize = (endImageSize * resources.displayMetrics.density).toInt()
    if (file != null) {
        Picasso.get().load(file)
            .resize(imageSize, imageSize)
            .into(target)
    } else {
        Picasso.get().cancelRequest(target)
        target.updateDrawable(null)
    }
}
