package org.cru.godtools.tract.ui.controller

import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.tool.parser.model.tract.Modal
import org.cru.godtools.tool.tract.databinding.TractContentModalBinding
import org.greenrobot.eventbus.EventBus

class ModalController @AssistedInject internal constructor(
    @Assisted private val binding: TractContentModalBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Modal>(Modal::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: TractContentModalBinding, lifecycleOwner: LifecycleOwner, toolState: State): ModalController
    }

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override val childContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.modal = model
    }
}

internal fun TractContentModalBinding.bindController(
    factory: ModalController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, toolState)
