package org.cru.godtools.tool.tips.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.tips.TipPage
import org.cru.godtools.tool.state.State
import org.cru.godtools.tool.tips.databinding.ToolTipPageBinding
import org.greenrobot.eventbus.EventBus

class TipPageController @AssistedInject internal constructor(
    @Assisted private val binding: ToolTipPageBinding,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    cacheFactory: UiControllerCache.Factory
) : ParentController<TipPage>(TipPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: ToolTipPageBinding, toolState: State): TipPageController
    }

    init {
        binding.controller = this
    }

    override val lifecycleOwner get() = binding.lifecycleOwner
    override val childContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.page = model
    }
}

fun ToolTipPageBinding.bindController(factory: TipPageController.Factory, toolState: State) =
    controller ?: factory.create(this, toolState)
