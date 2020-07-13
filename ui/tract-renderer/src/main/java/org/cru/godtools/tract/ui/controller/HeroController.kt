package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractPageHeroBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Hero

class HeroController internal constructor(private val binding: TractPageHeroBinding, pageController: PageController) :
    ParentController<Hero>(Hero::class, binding.root, pageController) {
    init {
        binding.controller = this
    }

    override val contentContainer get() = binding.content
    private var pendingAnalyticsEvents: List<Runnable>? = null

    // region Lifecycle
    override fun onVisible() {
        super.onVisible()
        pendingAnalyticsEvents =
            model?.let { triggerAnalyticsEvents(it.analyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT) }
    }

    override fun onHidden() {
        super.onHidden()
        pendingAnalyticsEvents?.let { cancelPendingAnalyticsEvents(it) }
    }
    // endregion Lifecycle
}

fun TractPageHeroBinding.bindController(pageController: PageController) =
    controller ?: HeroController(this, pageController)
