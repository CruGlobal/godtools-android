package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.tract.databinding.TractContentTextBinding
import org.cru.godtools.xml.model.Text

internal class TextController private constructor(
    private val binding: TractContentTextBinding,
    parentController: BaseController<*>
) : BaseController<Text>(Text::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(TractContentTextBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<TextController>

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
