package org.cru.godtools.base.tool.databinding.adapters

import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import org.ccci.gto.android.common.lottie.setAnimationFromFile
import org.cru.godtools.base.tool.model.getFileBlocking
import org.cru.godtools.base.toolFileSystem
import org.cru.godtools.shared.tool.parser.model.Resource

@BindingAdapter("animation")
internal fun LottieAnimationView.setAnimation(resource: Resource?) {
    resource?.getFileBlocking(context.toolFileSystem)?.let { setAnimationFromFile(it) }
}
