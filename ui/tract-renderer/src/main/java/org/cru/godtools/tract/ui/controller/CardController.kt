package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractContentCardBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Card

class CardController private constructor(
    private val binding: TractContentCardBinding,
    pageController: PageController
) : ParentController<Card>(Card::class, binding.root, pageController) {
    constructor(parent: ViewGroup, pageController: PageController) :
        this(TractContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false), pageController)

    interface Callbacks {
        fun onToggleCard(controller: CardController)
        fun onPreviousCard()
        fun onNextCard()
        fun onDismissCard(controller: CardController)
    }

    override val lifecycleOwner =
        pageController.lifecycleOwner?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.STARTED) }

    private val callbacks: Callbacks? = pageController
    private var pendingAnalyticsEvents: List<Job>? = null

    init {
        binding.controller = this
        binding.enableTips = pageController.isTipsEnabled

        lifecycleOwner?.lifecycle?.apply {
            onResume {
                pendingAnalyticsEvents =
                    triggerAnalyticsEvents(model?.analyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT)
            }
            onPause { pendingAnalyticsEvents?.cancelPendingAnalyticsEvents() }
        }
    }

    // region Lifecycle
    @CallSuper
    override fun onBind() {
        super.onBind()
        binding.model = model
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        processDismissEvent(event)
    }
    // endregion Lifecycle

    override val contentContainer get() = binding.content

    private fun processDismissEvent(event: Event) {
        if (model?.dismissListeners?.contains(event.id) == true) dismissCard()
    }

    fun toggleCard() = callbacks?.onToggleCard(this)
    fun nextCard() = callbacks?.onNextCard()
    fun previousCard() = callbacks?.onPreviousCard()
    private fun dismissCard() = callbacks?.onDismissCard(this)
}
