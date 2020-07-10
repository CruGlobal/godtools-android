package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractContentCardBinding
import org.cru.godtools.tract.viewmodel.PageViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Card

class CardController private constructor(
    private val binding: TractContentCardBinding,
    pageViewHolder: PageViewHolder?
) : ParentController<Card>(Card::class, binding.root, pageViewHolder) {
    constructor(parent: ViewGroup, pageViewHolder: PageViewHolder?) :
        this(TractContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false), pageViewHolder)

    interface Callbacks {
        fun onToggleCard(controller: CardController)
        fun onPreviousCard()
        fun onNextCard()
        fun onDismissCard(controller: CardController)
    }

    init {
        binding.controller = this
    }

    internal var callbacks: Callbacks? = pageViewHolder
    private var pendingAnalyticsEvents: List<Runnable>? = null

    // region Lifecycle
    @CallSuper
    override fun onBind() {
        super.onBind()
        binding.model = model
    }

    override fun onVisible() {
        super.onVisible()
        pendingAnalyticsEvents =
            model?.let { triggerAnalyticsEvents(it.analyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT) }
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        processDismissEvent(event)
    }

    override fun onHidden() {
        super.onHidden()
        pendingAnalyticsEvents?.let { cancelPendingAnalyticsEvents(it) }
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
