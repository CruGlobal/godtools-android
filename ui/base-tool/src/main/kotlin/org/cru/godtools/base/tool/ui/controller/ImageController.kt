package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.tool.databinding.ToolContentImageBinding
import org.cru.godtools.tool.model.Image

internal class ImageController private constructor(
    private val binding: ToolContentImageBinding,
    parentController: BaseController<*>
) : BaseController<Image>(Image::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentImageBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<ImageController>

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
        binding.isGone = model?.isGoneFlow(toolState)?.asLiveData()
        binding.isInvisible = model?.isInvisibleFlow(toolState)?.asLiveData()
    }
}
