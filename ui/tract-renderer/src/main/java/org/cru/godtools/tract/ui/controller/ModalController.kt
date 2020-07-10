package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractContentModalBinding
import org.cru.godtools.xml.model.Modal

class ModalController internal constructor(private val binding: TractContentModalBinding) :
    ParentController<Modal>(Modal::class, binding.root, null) {
    init {
        binding.controller = this
    }

    override val contentContainer get() = binding.content

    override fun onBind() {
        super.onBind()
        binding.modal = model
    }
}

internal fun TractContentModalBinding.bindController() = controller ?: ModalController(this)
