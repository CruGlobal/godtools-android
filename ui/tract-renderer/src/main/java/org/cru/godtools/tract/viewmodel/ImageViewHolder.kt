package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import androidx.annotation.UiThread
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractContentImageBinding
import org.cru.godtools.xml.model.Image

@UiThread
internal class ImageViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Image>(Image::class.java, parent, R.layout.tract_content_image, parentViewHolder) {
    private val binding = TractContentImageBinding.bind(mRoot).also { it.holder = this }

    public override fun onBind() {
        super.onBind()
        binding.model = mModel
    }
}
