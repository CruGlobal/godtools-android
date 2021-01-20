package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentVideoBinding
import org.cru.godtools.xml.model.Video

internal class VideoController private constructor(
    private val binding: ToolContentVideoBinding,
    parentController: BaseController<*>
) : BaseController<Video>(Video::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<VideoController>

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
