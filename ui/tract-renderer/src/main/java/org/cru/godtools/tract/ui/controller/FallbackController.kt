package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.tract.databinding.TractContentFallbackBinding
import org.cru.godtools.xml.model.Fallback

class FallbackController(
    binding: TractContentFallbackBinding,
    parentController: BaseController<*>,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Fallback>(Fallback::class, binding.root, parentController, cacheFactory) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : this(
        TractContentFallbackBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        cacheFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<FallbackController>

    override val contentContainer = binding.root
    override val contentToRender get() = model?.content?.take(1)
}
