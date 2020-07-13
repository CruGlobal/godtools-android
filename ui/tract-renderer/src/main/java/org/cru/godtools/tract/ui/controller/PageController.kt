package org.cru.godtools.tract.ui.controller

import android.content.SharedPreferences
import android.view.View
import androidx.annotation.UiThread
import androidx.core.util.Pools
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_CLICKED
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_SWIPED
import org.cru.godtools.base.Settings.Companion.PREF_FEATURE_DISCOVERED
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.tract.viewmodel.PageViewHolder
import org.cru.godtools.xml.model.Card

class PageController(private val binding: TractPageBinding) : PageViewHolder(binding), CardController.Callbacks,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val heroController = binding.hero.bindController(this)
    // TODO: this should be injected via Dagger
    private val settings = Settings.getInstance(binding.root.context)

    init {
        binding.controller = this
        binding.pageContentLayout.setActiveCardListener(this)
    }

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        heroController.bind(model?.hero)
        updateVisibleCards()
    }

    override fun onVisible() {
        settings.registerOnSharedPreferenceChangeListener(this)
        super.onVisible()
        mActiveCardViewHolder?.markVisible() ?: heroController.markVisible()
        updateBounceAnimation()
    }

    fun onLiveShareNavigationEvent(event: NavigationEvent) {
        if (model?.position != event.page) return

        when (val card = event.card?.let { model?.cards?.getOrNull(it) }) {
            null -> binding.pageContentLayout.changeActiveCard(null, true)
            else -> displayCard(card)
        }
    }

    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        checkForCardEvent(event)
        propagateEventToChildren(event)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "$PREF_FEATURE_DISCOVERED$FEATURE_TRACT_CARD_CLICKED",
            "$PREF_FEATURE_DISCOVERED$FEATURE_TRACT_CARD_SWIPED" -> updateBounceAnimation()
        }
    }

    override fun onHidden() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onHidden()
        mActiveCardViewHolder?.markHidden() ?: heroController.markHidden()
        updateBounceAnimation()
    }
    // endregion Lifecycle

    // region Cards
    private var enabledHiddenCards = mutableSetOf<String>()
    private var visibleCards = emptyList<Card>()
    private var cardControllers = emptyList<CardController>()
    val activeCard get() = mActiveCardViewHolder?.model

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

    override fun hideHiddenCardsThatArentActive() {
        if (enabledHiddenCards.isEmpty()) return
        if (enabledHiddenCards.removeAll { it != activeCard?.id }) updateVisibleCards()
    }

    override fun updateVisibleCard(old: CardController?) {
        if (!mVisible || old == mActiveCardViewHolder) return

        old?.markHidden() ?: heroController.markHidden()
        mActiveCardViewHolder?.markVisible() ?: heroController.markVisible()
    }

    @UiThread
    private fun updateVisibleCards() {
        visibleCards = model?.cards?.filter { isCardVisible(it) }.orEmpty()
        bindCards()
    }

    private var cardsNeedRebind = false

    @UiThread
    private tailrec fun bindCards() {
        // short-circuit since we are already binding cards
        if (mBindingCards) {
            cardsNeedRebind = true
            return
        }

        val parent = binding.pageContentLayout
        val invalid: View = parent // We just need a non-null placeholder value that can't be a card view
        var activeCard = if (parent.activeCard != null) invalid else null
        try {
            mBindingCards = true
            cardsNeedRebind = false

            // map old view holders to new location
            var lastNewPos = -1
            val holders = arrayOfNulls<CardController>(visibleCards.size)
            cardControllers.forEach { holder ->
                when (val newPos = visibleCards.indexOfFirst { it.id == holder.model?.id }) {
                    -1 -> {
                        // recycle this view holder
                        parent.removeView(holder.mRoot)
                        holder.bind(null)
                        recycledCardControllers.release(holder)
                    }
                    else -> {
                        holders[newPos] = holder

                        // is this the active card? if so track it to restore it after we finish binding
                        if (activeCard === invalid && parent.activeCard === holder.mRoot) {
                            activeCard = holder.mRoot
                        }

                        when {
                            // remove this view for now, we will re-add it shortly
                            lastNewPos > newPos -> parent.removeView(holder.mRoot)
                            else -> lastNewPos = newPos
                        }
                    }
                }
            }

            // create and bind any needed view holders
            cardControllers = holders.map { it ?: recycledCardControllers.acquire() ?: CardController(parent, this) }
            cardControllers.forEachIndexed { i, it ->
                it.bind(visibleCards.getOrNull(i))
                if (parent !== it.mRoot.parent) parent.addCard(it.mRoot, i)
            }
        } finally {
            // finished binding cards
            mBindingCards = false
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

    // region CardController.Callbacks
    override fun onToggleCard(controller: CardController) {
        settings.setFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED)
        binding.pageContentLayout.apply {
            changeActiveCard(controller.mRoot.takeUnless { mRoot === activeCard }, true)
        }
    }

    override fun onPreviousCard() {
        binding.pageContentLayout.apply { changeActiveCard(activeCardPosition - 1, true) }
    }

    override fun onNextCard() {
        binding.pageContentLayout.apply { changeActiveCard(activeCardPosition + 1, true) }
    }

    override fun onDismissCard(controller: CardController) {
        binding.pageContentLayout.apply {
            if (activeCard == controller.mRoot) changeActiveCard(null, true)
        }
    }
    // endregion CardController.Callbacks
    // endregion Cards

    // region Content Events
    private fun checkForCardEvent(event: Event) {
        model?.cards?.firstOrNull { event.id in it.listeners }
            ?.let { displayCard(it) }
    }

    private fun propagateEventToChildren(event: Event) {
        mActiveCardViewHolder?.onContentEvent(event) ?: heroController.onContentEvent(event)
    }
    // endregion Content Events

    // TODO: move this into data binding and use LiveData once we attach the PageBinding to a LifecycleOwner
    private fun updateBounceAnimation() {
        // we bounce the first card if the page is visible and the user hasn't opened a card before
        binding.pageContentLayout.setBounceFirstCard(
            mVisible && !(settings.isFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED) ||
                settings.isFeatureDiscovered(FEATURE_TRACT_CARD_SWIPED))
        )
    }
}

fun TractPageBinding.bindController() = controller ?: PageController(this)
