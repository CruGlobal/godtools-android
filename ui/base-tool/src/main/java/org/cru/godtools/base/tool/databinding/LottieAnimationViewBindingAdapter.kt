package org.cru.godtools.base.tool.databinding

import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import org.ccci.gto.android.common.lottie.setAnimationFromFile
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.xml.model.Resource

@BindingAdapter("animation")
internal fun LottieAnimationView.setAnimation(resource: Resource?) {
    resource?.localName?.let { setAnimationFromFile(context.toolFileManager.getFileBlocking(it)) }
}
