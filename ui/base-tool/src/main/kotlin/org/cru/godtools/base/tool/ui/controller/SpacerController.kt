package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.shared.tool.parser.model.Spacer
import org.cru.godtools.tool.databinding.ToolContentSpacerBinding

class SpacerController private constructor(
    private val binding: ToolContentSpacerBinding,
    parentController: BaseController<*>
) : BaseController<Spacer>(Spacer::class, binding.root, parentController) {
    @AssistedInject
    internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentSpacerBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<SpacerController>

    override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
