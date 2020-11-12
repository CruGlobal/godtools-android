package org.cru.godtools.tract.ui.controller.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentInlineTipBinding
import org.cru.godtools.tract.ui.controller.BaseController
import org.cru.godtools.xml.model.tips.InlineTip

internal class InlineTipController private constructor(
    private val binding: TractContentInlineTipBinding,
    parentController: BaseController<*>?
) : BaseController<InlineTip>(InlineTip::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentInlineTipBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.enableTips = isTipsEnabled
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
        binding.isCompleted = isTipComplete(model?.id)
    }
}
