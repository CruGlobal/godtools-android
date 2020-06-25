package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractPageHeroBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.PageViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Hero

class HeroController internal constructor(private val binding: TractPageHeroBinding, parentViewHolder: PageViewHolder) :
    ParentController<Hero>(Hero::class.java, binding.root, parentViewHolder) {
    override val contentContainer get() = binding.content
    private var pendingAnalyticsEvents: List<Runnable>? = null

    // region Lifecycle
    override fun onVisible() {
        super.onVisible()
        model?.let {
            pendingAnalyticsEvents = triggerAnalyticsEvents(it.analyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT)
        }
    }

    override fun onHidden() {
        super.onHidden()
        pendingAnalyticsEvents?.let { cancelPendingAnalyticsEvents(it) }
    }
    // endregion Lifecycle
}

// TODO: this may change once I figure out what code pattern I want to use to create/bind controllers
fun TractPageHeroBinding.bindController(pageController: PageViewHolder) =
    BaseViewHolder.forView(root, HeroController::class.java) ?: HeroController(this, pageController)
