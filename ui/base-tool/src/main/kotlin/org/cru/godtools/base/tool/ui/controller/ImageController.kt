package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.isGoneStateFlow
import org.cru.godtools.base.tool.model.isInvisibleStateFlow
import org.cru.godtools.shared.tool.parser.model.Image
import org.cru.godtools.tool.databinding.ToolContentImageBinding

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
        binding.isGone = model?.isGoneStateFlow(toolState, lifecycleOwner.lifecycleScope)
        binding.isInvisible = model?.isInvisibleStateFlow(toolState, lifecycleOwner.lifecycleScope)
    }
}
