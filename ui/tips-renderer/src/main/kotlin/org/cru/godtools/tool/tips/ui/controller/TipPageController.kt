package org.cru.godtools.tool.tips.ui.controller

import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.tool.parser.model.tips.TipPage
import org.cru.godtools.shared.tool.state.State
import org.cru.godtools.tool.tips.databinding.ToolTipPageBinding
import org.greenrobot.eventbus.EventBus

class TipPageController @AssistedInject internal constructor(
    @Assisted private val binding: ToolTipPageBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    cacheFactory: UiControllerCache.Factory
) : ParentController<TipPage>(TipPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: ToolTipPageBinding, lifecycleOwner: LifecycleOwner, toolState: State): TipPageController
    }

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override val childContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.page = model
    }
}

fun ToolTipPageBinding.bindController(
    factory: TipPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, toolState)
