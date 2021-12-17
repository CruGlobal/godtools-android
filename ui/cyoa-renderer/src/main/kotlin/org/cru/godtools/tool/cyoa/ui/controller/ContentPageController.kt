package org.cru.godtools.tool.cyoa.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.model.AnalyticsEvent.Trigger
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

    init {
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
    }

    override val lifecycleOwner = binding.lifecycleOwner
    override val childContainer = binding.content

    // region Analytics Events
    private var pendingVisibleAnalyticsEvents: List<Job>? = null

    init {
        lifecycleOwner?.lifecycle?.apply {
            onResume {
                pendingVisibleAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE))
            }
            onPause {
                pendingVisibleAnalyticsEvents?.cancelPendingAnalyticsEvents()
                triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.HIDDEN))
            }
        }
    }
    // endregion Analytics Events
}

fun CyoaPageContentBinding.bindController(factory: ContentPageController.Factory, toolState: State) =
    controller ?: factory.create(this, toolState)
