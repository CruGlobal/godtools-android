package org.cru.godtools.tool.cyoa.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionBinding
import org.cru.godtools.tool.model.page.CardCollectionPage
import org.cru.godtools.tool.state.State
import org.greenrobot.eventbus.EventBus

class CardCollectionPageController @AssistedInject constructor(
    @Assisted private val binding: CyoaPageCardCollectionBinding,
    @Assisted override val toolState: State,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus
) : BaseController<CardCollectionPage>(CardCollectionPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: CyoaPageCardCollectionBinding, toolState: State): CardCollectionPageController
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
    }

    override val lifecycleOwner = binding.lifecycleOwner
}

fun CyoaPageCardCollectionBinding.bindController(factory: CardCollectionPageController.Factory, toolState: State) =
    controller ?: factory.create(this, toolState)
