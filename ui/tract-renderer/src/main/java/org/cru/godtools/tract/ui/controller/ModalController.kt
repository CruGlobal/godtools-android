package org.cru.godtools.tract.ui.controller

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.cru.godtools.tract.databinding.TractContentModalBinding
import org.cru.godtools.xml.model.Modal
import org.greenrobot.eventbus.EventBus

class ModalController @AssistedInject internal constructor(
    @Assisted private val binding: TractContentModalBinding,
    override val eventBus: EventBus
) : ParentController<Modal>(Modal::class, binding.root, null) {
    @AssistedInject.Factory
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
