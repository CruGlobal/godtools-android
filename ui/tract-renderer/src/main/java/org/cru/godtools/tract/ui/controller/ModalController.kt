package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractContentModalBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.ParentViewHolder
import org.cru.godtools.xml.model.Modal

class ModalController internal constructor(private val binding: TractContentModalBinding) :
    ParentViewHolder<Modal>(Modal::class.java, binding.root, null) {
    override fun onBind() {
        super.onBind()
        binding.modal = model
    }
}

// TODO: this may change once I figure out what code pattern I want to use to create/bind controllers
internal fun TractContentModalBinding.bindController() =
    BaseViewHolder.forView(root, ModalController::class.java) ?: ModalController(this)
