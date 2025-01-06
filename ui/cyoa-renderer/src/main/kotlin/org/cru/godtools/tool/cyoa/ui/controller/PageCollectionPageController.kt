package org.cru.godtools.tool.cyoa.ui.controller

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.tool.parser.model.page.PageCollectionPage
import org.cru.godtools.shared.tool.state.State
import org.cru.godtools.tool.cyoa.databinding.CyoaPagePageCollectionBinding
import org.cru.godtools.tool.tips.ShowTipCallback
import org.greenrobot.eventbus.EventBus

class PageCollectionPageController @AssistedInject constructor(
    @Assisted private val binding: CyoaPagePageCollectionBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val enableTips: LiveData<Boolean>,
    @Assisted override val toolState: State,
    eventBus: EventBus,
) : BaseController<PageCollectionPage>(PageCollectionPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(
            binding: CyoaPagePageCollectionBinding,
            lifecycleOwner: LifecycleOwner,
            enableTips: LiveData<Boolean>,
            toolState: State,
        ): PageCollectionPageController
    }

    internal var callbacks: ShowTipCallback? = null

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
    }
}

fun CyoaPagePageCollectionBinding.bindController(
    factory: PageCollectionPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    enableTips: LiveData<Boolean>,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, enableTips, toolState)
