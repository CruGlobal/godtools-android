package org.cru.godtools.base.tool.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
import org.ccci.gto.android.common.picasso.material.button.MaterialButtonIconTarget
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.dagger.picasso
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.tool.model.ImageGravity
import org.cru.godtools.tool.model.Resource

@BindingAdapter("icon", "iconSize")
fun MaterialButton.bindIconResource(resource: Resource?, size: Int) {
    val target = MaterialButtonIconTarget.of(this)
    val file = resource?.localName?.let { context.toolFileManager.getFileBlocking(it) }
    val imageSize = dpToPixelSize(size, resources)
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

@BindingAdapter("iconGravity")
fun MaterialButton.bindIconGravity(gravity: ImageGravity) {
    iconGravity = when {
        gravity.isEnd -> ICON_GRAVITY_TEXT_END
        gravity.isStart -> ICON_GRAVITY_TEXT_START
        else -> ICON_GRAVITY_TEXT_START
    }
}
