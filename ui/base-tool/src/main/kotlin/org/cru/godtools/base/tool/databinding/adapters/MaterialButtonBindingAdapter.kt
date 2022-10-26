package org.cru.godtools.base.tool.databinding.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
import org.ccci.gto.android.common.picasso.material.button.MaterialButtonIconTarget
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.dagger.picasso
import org.cru.godtools.base.tool.model.getFileBlocking
import org.cru.godtools.base.toolFileSystem
import org.cru.godtools.shared.tool.parser.model.Gravity
import org.cru.godtools.shared.tool.parser.model.Resource

@BindingAdapter("icon", "iconSize")
internal fun MaterialButton.bindIconResource(resource: Resource?, size: Int) {
    val target = MaterialButtonIconTarget.of(this)
    val file = resource?.getFileBlocking(context.toolFileSystem)
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
internal fun MaterialButton.bindIconGravity(gravity: Gravity.Horizontal) {
    iconGravity = when (gravity) {
        Gravity.Horizontal.END -> ICON_GRAVITY_TEXT_END
        Gravity.Horizontal.START -> ICON_GRAVITY_TEXT_START
        else -> ICON_GRAVITY_TEXT_START
    }
}
