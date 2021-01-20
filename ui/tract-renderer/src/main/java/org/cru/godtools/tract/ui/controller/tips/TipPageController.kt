package org.cru.godtools.tract.ui.controller.tips

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.UiControllerCache
import org.cru.godtools.tract.databinding.TractTipPageBinding
import org.cru.godtools.tract.ui.tips.TipCallbacks
import org.cru.godtools.xml.model.tips.TipPage
import org.greenrobot.eventbus.EventBus

class TipPageController @AssistedInject internal constructor(
    @Assisted private val binding: TractTipPageBinding,
    override val eventBus: EventBus,
    cacheFactory: UiControllerCache.Factory
) : ParentController<TipPage>(TipPage::class, binding.root, cacheFactory = cacheFactory) {
    @AssistedFactory
    interface Factory {
        fun create(binding: TractTipPageBinding): TipPageController
    }

    init {
        binding.controller = this
    }

    override val lifecycleOwner get() = binding.lifecycleOwner

    var callbacks: TipCallbacks?
        get() = binding.callbacks
        set(value) {
            binding.callbacks = value
        }

    override val contentContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.page = model
    }
}

fun TractTipPageBinding.bindController(factory: TipPageController.Factory) = controller ?: factory.create(this)
