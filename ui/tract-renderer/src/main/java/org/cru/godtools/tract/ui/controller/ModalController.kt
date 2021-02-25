package org.cru.godtools.tract.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tract.databinding.TractContentModalBinding
import org.cru.godtools.xml.model.tract.Modal
import org.greenrobot.eventbus.EventBus

class ModalController @AssistedInject internal constructor(
    @Assisted private val binding: TractContentModalBinding,
    eventBus: EventBus,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Modal>(Modal::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: TractContentModalBinding): ModalController
    }

    init {
        binding.controller = this
    }

    override val contentContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.modal = model
    }
}

internal fun TractContentModalBinding.bindController(factory: ModalController.Factory) =
    controller ?: factory.create(this)
