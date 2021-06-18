package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentTextBinding
import org.cru.godtools.tool.model.Text

internal class TextController private constructor(
    private val binding: ToolContentTextBinding,
    parentController: BaseController<*>
) : BaseController<Text>(Text::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentTextBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<TextController>

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
