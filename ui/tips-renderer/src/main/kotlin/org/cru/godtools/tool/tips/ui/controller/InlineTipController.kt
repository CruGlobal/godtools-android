package org.cru.godtools.tool.tips.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.shared.tool.parser.model.tips.InlineTip
import org.cru.godtools.tool.tips.databinding.ToolContentInlineTipBinding

internal class InlineTipController private constructor(
    private val binding: ToolContentInlineTipBinding,
    parentController: BaseController<*>,
    private val tipsRepository: TrainingTipsRepository,
) : BaseController<InlineTip>(InlineTip::class, binding.root, parentController) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        tipsRepository: TrainingTipsRepository,
    ) : this(
        ToolContentInlineTipBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        tipsRepository,
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<InlineTipController>

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.enableTips = enableTips
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
        binding.isCompleted = tipsRepository.isTipComplete(model?.id)
    }
}
