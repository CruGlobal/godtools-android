package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.isGoneStateFlow
import org.cru.godtools.base.tool.model.isInvisibleStateFlow
import org.cru.godtools.shared.tool.parser.model.Text
import org.cru.godtools.tool.databinding.ToolContentTextBinding

internal class TextController private constructor(
    private val binding: ToolContentTextBinding,
    parentController: BaseController<*>
) : BaseController<Text>(Text::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentTextBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<TextController>

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
        binding.isInvisible = model?.isInvisibleStateFlow(toolState, lifecycleOwner.lifecycleScope)
        binding.isGone = model?.isGoneStateFlow(toolState, lifecycleOwner.lifecycleScope)
    }
}
