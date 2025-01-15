package org.cru.godtools.tract.ui.controller

import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.core.util.Pools
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.ccci.gto.android.common.androidx.lifecycle.or
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_CLICKED
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_SWIPED
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.shared.tool.parser.model.tract.Modal
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card
import org.cru.godtools.shared.tool.state.State
import org.cru.godtools.tool.tips.ShowTipCallback
import org.cru.godtools.tool.tract.databinding.TractPageBinding
import org.cru.godtools.tract.widget.PageContentLayout
import org.greenrobot.eventbus.EventBus

class PageController @AssistedInject internal constructor(
    @Assisted private val binding: TractPageBinding,
    @Assisted override val lifecycleOwner: ConstrainedStateLifecycleOwner,
    @Assisted override val enableTips: LiveData<Boolean>,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    private val settings: Settings,
    private val tipsRepository: TrainingTipsRepository,
    heroControllerFactory: HeroController.Factory,
    private val cardControllerFactory: CardController.Factory
) : BaseController<TractPage>(TractPage::class, binding.root, eventBus = eventBus),
    CardController.Callbacks,
    PageContentLayout.OnActiveCardListener {

    @AssistedFactory
    interface Factory {
        fun create(
            binding: TractPageBinding,
            lifecycleOwner: ConstrainedStateLifecycleOwner,
            enableTips: LiveData<Boolean>,
            toolState: State
        ): PageController
    }

    interface Callbacks : ShowTipCallback {
        fun onUpdateActiveCard(page: TractPage?, card: Card?)
        fun showModal(modal: Modal)
        fun goToNextPage()
    }

    @VisibleForTesting
    internal val heroController = heroControllerFactory.create(binding.hero, this)
    var callbacks: Callbacks?
        get() = binding.callbacks
        set(value) {
            binding.callbacks = value
        }

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.cardsDiscovered = settings.isFeatureDiscoveredLiveData(FEATURE_TRACT_CARD_CLICKED) or
            settings.isFeatureDiscoveredLiveData(FEATURE_TRACT_CARD_SWIPED)
        binding.pageContentLayout.activeCardListener = this
        binding.enableTips = enableTips

        with(lifecycleOwner.lifecycle) {
            onResume { binding.isVisible = true }
            onPause { binding.isVisible = false }
        }
    }

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        binding.page = model
        binding.isHeaderTipComplete = tipsRepository.isTipComplete(model?.header?.tip?.id)
        binding.isCallToActionTipComplete = tipsRepository.isTipComplete(model?.callToAction?.tip?.id)
        heroController.model = model?.hero
        updateVisibleCards()
    }

    fun onLiveShareNavigationEvent(event: NavigationEvent) {
        if (model?.position != event.page) return

        when (val card = event.card?.let { model?.cards?.getOrNull(it) }) {
            null -> binding.pageContentLayout.changeActiveCard(null, true)
            else -> displayCard(card)
        }
    }

    override fun onContentEvent(event: Event) {
        checkForModalEvent(event)
        checkForCardEvent(event)
        propagateEventToChildren(event)
    }
    // endregion Lifecycle

    // region Cards
    private var enabledHiddenCards = mutableSetOf<String>()
    private var visibleCards = emptyList<Card>()
    @VisibleForTesting
    internal var cardControllers = emptyList<CardController>()
        private set
    private var activeCardController: CardController? = null
    val activeCard get() = activeCardController?.model

    private val recycledCardControllers = Pools.SimplePool<CardController>(3)

    private fun isCardVisible(card: Card) = !card.isHidden || card.id in enabledHiddenCards

    private fun displayCard(card: Card) {
        if (card.isHidden) {
            enabledHiddenCards.add(card.id)
            updateVisibleCards()
        }

        // navigate to the specified card
        val i = visibleCards.indexOfFirst { it.id == card.id }
        if (i != -1) binding.pageContentLayout.changeActiveCard(i, true)
    }

    private fun hideHiddenCardsThatArentActive() {
        if (enabledHiddenCards.isEmpty()) return
        if (enabledHiddenCards.removeAll { it != activeCard?.id }) updateVisibleCards()
    }

    private fun updateChildrenLifecycles(old: CardController?, new: CardController?) {
        if (old == new) return

        (if (old != null) old.lifecycleOwner else heroController.lifecycleOwner)?.maxState = Lifecycle.State.STARTED
        (if (new != null) new.lifecycleOwner else heroController.lifecycleOwner)?.maxState = Lifecycle.State.RESUMED
    }

    @UiThread
    private fun updateVisibleCards() {
        visibleCards = model?.cards?.filter { isCardVisible(it) }.orEmpty()
        bindCards()
    }

    private var bindingCards = false
    private var cardsNeedRebind = false

    @UiThread
    private tailrec fun bindCards() {
        // short-circuit since we are already binding cards
        if (bindingCards) {
            cardsNeedRebind = true
            return
        }

        val parent = binding.pageContentLayout
        val invalid: View = parent // We just need a non-null placeholder value that can't be a card view
        var activeCard = if (parent.activeCard != null) invalid else null
        try {
            bindingCards = true
            cardsNeedRebind = false

            // map old view holders to new location
            var lastNewPos = -1
            val holders = arrayOfNulls<CardController>(visibleCards.size)
            cardControllers.forEach { holder ->
                when (val newPos = visibleCards.indexOfFirst { it.id == holder.model?.id }) {
                    -1 -> {
                        // recycle this view holder
                        parent.removeView(holder.root)
                        holder.model = null
                        recycledCardControllers.release(holder)
                    }
                    else -> {
                        holders[newPos] = holder

                        // is this the active card? if so track it to restore it after we finish binding
                        if (activeCard === invalid && parent.activeCard === holder.root) {
                            activeCard = holder.root
                        }

                        when {
                            // remove this view for now, we will re-add it shortly
                            lastNewPos > newPos -> parent.removeView(holder.root)
                            else -> lastNewPos = newPos
                        }
                    }
                }
            }

            // create and bind any needed view holders
            cardControllers = holders.map {
                it ?: recycledCardControllers.acquire() ?: cardControllerFactory.create(parent, this)
            }
            cardControllers.forEachIndexed { i, it ->
                it.model = visibleCards.getOrNull(i)
                if (parent !== it.root.parent) parent.addCard(it.root, i)
            }
        } finally {
            // finished binding cards
            bindingCards = false
        }

        when {
            // restore the active card
            activeCard !== invalid -> parent.changeActiveCard(activeCard, false)
            // trigger onActiveCard in case the active card changed during binding
            else -> onActiveCardChanged(parent.activeCard)
        }

        // rebind cards if a request to bind happened while we were already binding
        if (cardsNeedRebind) bindCards()
    }

    // region PageContentLayout.OnActiveCardListener
    override fun onActiveCardChanged(activeCard: View?) {
        if (bindingCards) return

        val old = activeCardController
        activeCardController = activeCard?.let { cardControllers.firstOrNull { it.root == activeCard } }
        hideHiddenCardsThatArentActive()
        updateChildrenLifecycles(old, activeCardController)
        callbacks?.onUpdateActiveCard(model, activeCardController?.model)
    }
    // endregion PageContentLayout.OnActiveCardListener

    // region CardController.Callbacks
    override fun onToggleCard(controller: CardController) {
        settings.setFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED)
        binding.pageContentLayout.apply { changeActiveCard(controller.root.takeUnless { it === activeCard }, true) }
    }

    override fun onPreviousCard() {
        binding.pageContentLayout.apply { changeActiveCard(activeCardPosition - 1, true) }
    }

    override fun onNextCard() {
        binding.pageContentLayout.apply { changeActiveCard(activeCardPosition + 1, true) }
    }

    override fun onDismissCard(controller: CardController) {
        binding.pageContentLayout.apply { if (activeCard == controller.root) changeActiveCard(null, true) }
    }
    // endregion CardController.Callbacks
    // endregion Cards

    // region Content Events
    private fun checkForModalEvent(event: Event) {
        model?.modals?.firstOrNull { event.id in it.listeners }
            ?.let { callbacks?.showModal(it) }
    }

    private fun checkForCardEvent(event: Event) {
        model?.cards?.firstOrNull { event.id in it.listeners }
            ?.let { displayCard(it) }
    }

    private fun propagateEventToChildren(event: Event) {
        activeCardController?.onContentEvent(event) ?: heroController.onContentEvent(event)
    }
    // endregion Content Events

    // region Tips
    override fun showTip(tip: Tip?) {
        tip?.let { callbacks?.showTip(tip) }
    }
    // endregion Tips

    override fun updateLayoutDirection() = Unit
}

internal fun TractPageBinding.bindController(
    factory: PageController.Factory,
    lifecycleOwner: ConstrainedStateLifecycleOwner,
    enableTips: LiveData<Boolean>,
    toolState: State
) = controller ?: factory.create(this, lifecycleOwner, enableTips, toolState)
