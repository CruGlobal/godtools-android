package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentImageBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Image

internal class ImageController private constructor(
    private val binding: TractContentImageBinding,
    parentViewHolder: BaseViewHolder<*>?
) : BaseViewHolder<Image>(Image::class.java, binding.root, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentImageBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
