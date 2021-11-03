package org.cru.godtools.tool.cyoa.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.model.page.ContentPage
import org.cru.godtools.tool.state.State
import org.greenrobot.eventbus.EventBus

class ContentPageController @AssistedInject constructor(
    @Assisted private val binding: CyoaPageContentBinding,
    @Assisted override val toolState: State,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus
) : ParentController<ContentPage>(ContentPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: CyoaPageContentBinding, toolState: State): ContentPageController
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
    }

    override val lifecycleOwner = binding.lifecycleOwner
    override val childContainer = binding.content
}

fun CyoaPageContentBinding.bindController(factory: ContentPageController.Factory, toolState: State) =
    controller ?: factory.create(this, toolState)
