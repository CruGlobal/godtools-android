package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.isGoneStateFlow
import org.cru.godtools.base.tool.model.isInvisibleStateFlow
import org.cru.godtools.shared.tool.parser.model.Base
import org.cru.godtools.shared.tool.parser.model.Button
import org.cru.godtools.tool.BR
import org.cru.godtools.tool.databinding.ToolContentButtonBinding
import org.cru.godtools.tool.databinding.ToolContentButtonOutlinedBinding
import org.greenrobot.eventbus.EventBus

internal sealed class ButtonController<T : ViewDataBinding>(
    private val binding: T,
    parentController: BaseController<*>,
    eventBus: EventBus
) : BaseController<Button>(Button::class, binding.root, parentController, eventBus) {
    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.setVariable(BR.controller, this)
    }

    public override fun onBind() {
        super.onBind()
        binding.setVariable(BR.model, model)
        binding.setVariable(BR.isGone, model?.isGoneStateFlow(toolState, lifecycleOwner.lifecycleScope))
        binding.setVariable(BR.isInvisible, model?.isInvisibleStateFlow(toolState, lifecycleOwner.lifecycleScope))
    }
}

internal class ContainedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>,
    eventBus: EventBus
) : ButtonController<ToolContentButtonBinding>(
    ToolContentButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController,
    eventBus
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<ContainedButtonController>

    override fun supportsModel(model: Base) =
        super.supportsModel(model) && model is Button && model.style == Button.Style.CONTAINED
}

internal class OutlinedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>,
    eventBus: EventBus
) : ButtonController<ToolContentButtonOutlinedBinding>(
    ToolContentButtonOutlinedBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController,
    eventBus
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<OutlinedButtonController>

    override fun supportsModel(model: Base) =
        super.supportsModel(model) && model is Button && model.style == Button.Style.OUTLINED
}
