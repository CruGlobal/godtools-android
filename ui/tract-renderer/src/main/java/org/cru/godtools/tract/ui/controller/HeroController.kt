package org.cru.godtools.tract.ui.controller

import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.tract.databinding.TractPageHeroBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Hero

class HeroController internal constructor(private val binding: TractPageHeroBinding, pageController: PageController) :
    ParentController<Hero>(Hero::class, binding.root, pageController) {
    override val lifecycleOwner = pageController.lifecycleOwner?.let { ConstrainedStateLifecycleOwner(it) }

    override val contentContainer get() = binding.content
    private var pendingAnalyticsEvents: List<Job>? = null

    init {
        binding.controller = this

        lifecycleOwner?.lifecycle?.apply {
            onResume {
                pendingAnalyticsEvents =
                    triggerAnalyticsEvents(model?.analyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT)
            }
            onPause { pendingAnalyticsEvents?.cancelPendingAnalyticsEvents() }
        }
    }
}

fun TractPageHeroBinding.bindController(pageController: PageController) =
    controller ?: HeroController(this, pageController)
