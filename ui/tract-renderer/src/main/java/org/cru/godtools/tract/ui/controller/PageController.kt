package org.cru.godtools.tract.ui.controller

import android.content.SharedPreferences
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.core.util.Pools
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_CLICKED
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_SWIPED
import org.cru.godtools.base.Settings.Companion.PREF_FEATURE_DISCOVERED
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.tract.widget.PageContentLayout
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Modal
import org.cru.godtools.xml.model.Page
import org.cru.godtools.xml.model.tips.Tip
import org.greenrobot.eventbus.EventBus

class PageController @AssistedInject internal constructor(
    @Assisted private val binding: TractPageBinding,
    @Assisted baseLifecycleOwner: LifecycleOwner?,
    override val eventBus: EventBus,
    private val settings: Settings
) : BaseController<Page>(Page::class, binding.root), CardController.Callbacks, PageContentLayout.OnActiveCardListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    @AssistedInject.Factory
    interface Factory {
        fun create(binding: TractPageBinding, lifecycleOwner: LifecycleOwner?): PageController
    }

    interface Callbacks {
        fun onUpdateActiveCard(page: Page?, card: Card?)
        fun showModal(modal: Modal)
        fun showTip(tip: Tip)
        fun goToNextPage()
    }

    override val lifecycleOwner =
        baseLifecycleOwner?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }
            .also { binding.lifecycleOwner = it }

    private val heroController = binding.hero.bindController(this)
    var callbacks: Callbacks?
        get() = binding.callbacks
        set(value) {
            binding.callbacks = value
        }

    init {
        binding.controller = this
        binding.pageContentLayout.setActiveCardListener(this)
    }

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        binding.page = model
        heroController.model = model?.hero
        updateVisibleCards()
    }

    override fun onVisible() {
        settings.registerOnSharedPreferenceChangeListener(this)
        super.onVisible()
        (activeCardController ?: heroController).isVisible = true
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
        checkForModalEvent(event)
        checkForCardEvent(event)
        propagateEventToChildren(event)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "$PREF_FEATURE_DISCOVERED$FEATURE_TRACT_CARD_CLICKED",
            "$PREF_FEATURE_DISCOVERED$FEATURE_TRACT_CARD_SWIPED" -> updateBounceAnimation()
            // key=null when preferences are cleared
            null -> updateBounceAnimation()
        }
    }

    override fun onHidden() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onHidden()
        (activeCardController ?: heroController).isVisible = false
        updateBounceAnimation()
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

    private fun updateVisibleCard(old: CardController?) {
        if (!isVisible || old == activeCardController) return

        (old ?: heroController).isVisible = false
        (activeCardController ?: heroController).isVisible = true
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
            cardControllers = holders.map { it ?: recycledCardControllers.acquire() ?: CardController(parent, this) }
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
        updateVisibleCard(old)
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
    override var isTipsEnabled: Boolean
        get() = binding.enableTips ?: false
        set(value) {
            binding.enableTips = value
        }
    override fun showTip(tip: Tip?) {
        tip?.let { callbacks?.showTip(tip) }
    }
    // endregion Tips

    override fun updateLayoutDirection() = Unit

    // TODO: move this into data binding and use LiveData once we attach the PageBinding to a LifecycleOwner
    private fun updateBounceAnimation() {
        // we bounce the first card if the page is visible and the user hasn't opened a card before
        binding.pageContentLayout.setBounceFirstCard(
            isVisible && !(settings.isFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED) ||
                settings.isFeatureDiscovered(FEATURE_TRACT_CARD_SWIPED))
        )
    }
}

internal fun TractPageBinding.bindController(factory: PageController.Factory, lifecycleOwner: LifecycleOwner? = null) =
    controller ?: factory.create(this, lifecycleOwner)
