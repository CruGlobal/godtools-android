package org.cru.godtools.tract.ui.controller.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.tool.model.tips.InlineTip
import org.cru.godtools.tract.databinding.TractContentInlineTipBinding
import org.keynote.godtools.android.db.GodToolsDao

internal class InlineTipController private constructor(
    private val binding: TractContentInlineTipBinding,
    parentController: BaseController<*>,
    private val dao: GodToolsDao
) : BaseController<InlineTip>(InlineTip::class, binding.root, parentController) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        dao: GodToolsDao
    ) : this(
        TractContentInlineTipBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        dao
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<InlineTipController>

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.enableTips = isTipsEnabled
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
        binding.isCompleted = dao.isTipComplete(model?.id)
    }
}
