package org.cru.godtools.base.tool.databinding

import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableEndTarget
import org.ccci.gto.android.common.picasso.widget.TextViewDrawableStartTarget
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.model.view.bindTo
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.xml.model.Resource
import org.cru.godtools.xml.model.Text

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
