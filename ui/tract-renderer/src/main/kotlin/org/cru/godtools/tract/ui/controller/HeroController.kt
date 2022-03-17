package org.cru.godtools.tract.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.AnalyticsEvent.Trigger
import org.cru.godtools.tool.model.tract.Hero
import org.cru.godtools.tract.databinding.TractPageHeroBinding

class HeroController @AssistedInject internal constructor(
    @Assisted private val binding: TractPageHeroBinding,
    @Assisted pageController: PageController,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Hero>(Hero::class, binding.root, pageController, cacheFactory = cacheFactory) {
    @AssistedFactory
    interface Factory {
        fun create(binding: TractPageHeroBinding, pageController: PageController): HeroController
    }

    override val lifecycleOwner = pageController.lifecycleOwner?.let { ConstrainedStateLifecycleOwner(it) }

    override val childContainer get() = binding.content
    private var pendingAnalyticsEvents: List<Job>? = null

    init {
        lifecycleOwner?.lifecycle?.apply {
            onResume { pendingAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE)) }
            onPause { pendingAnalyticsEvents?.cancelPendingAnalyticsEvents() }
        }
    }
}
