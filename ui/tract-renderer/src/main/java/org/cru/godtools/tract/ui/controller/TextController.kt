package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentTextBinding
import org.cru.godtools.xml.model.Text

internal class TextController private constructor(
    private val binding: TractContentTextBinding,
    parentController: BaseController<*>?
) : BaseController<Text>(Text::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentTextBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
