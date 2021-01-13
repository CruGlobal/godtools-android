package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentFallbackBinding
import org.cru.godtools.xml.model.Fallback

class FallbackController(
    binding: TractContentFallbackBinding,
    parentController: BaseController<*>?
) : ParentController<Fallback>(Fallback::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentFallbackBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    override val contentContainer = binding.root
    override val contentToRender get() = model?.content?.take(1)
}
