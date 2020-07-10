package org.cru.godtools.tract.ui.controller

import android.content.SharedPreferences
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
    }

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        heroController.bind(model?.hero)
    }

    override fun onVisible() {
        settings.registerOnSharedPreferenceChangeListener(this)
        super.onVisible()
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
        checkForCardEvent(event)
        super.onContentEvent(event)
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
        updateBounceAnimation()
    }
    // endregion Lifecycle

    // region Cards
    private var enabledHiddenCards = mutableSetOf<String>()
    val activeCard get() = mActiveCardViewHolder?.model

    override fun isCardVisible(card: Card) = !card.isHidden || card.id in enabledHiddenCards

    private fun displayCard(card: Card) {
        if (card.isHidden) {
            enabledHiddenCards.add(card.id)
            updateDisplayedCards()
        }

        // navigate to the specified card
        val i = mCards.indexOfFirst { it.id == card.id }
        if (i != -1) binding.pageContentLayout.changeActiveCard(i, true)
    }

    override fun hideHiddenCardsThatArentActive() {
        if (enabledHiddenCards.isEmpty()) return
        if (enabledHiddenCards.removeAll { it != activeCard?.id }) updateDisplayedCards()
    }

    override fun updateVisibleCard(old: CardController?) {
        if (!mVisible || old == mActiveCardViewHolder) return

        old?.markHidden() ?: heroController.markHidden()
        mActiveCardViewHolder?.markVisible() ?: heroController.markVisible()
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
