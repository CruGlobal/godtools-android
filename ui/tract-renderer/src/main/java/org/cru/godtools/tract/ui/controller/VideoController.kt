package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentVideoBinding
import org.cru.godtools.xml.model.Video

internal class VideoController private constructor(
    private val binding: TractContentVideoBinding,
    parentController: BaseController<*>?
) : BaseController<Video>(Video::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
