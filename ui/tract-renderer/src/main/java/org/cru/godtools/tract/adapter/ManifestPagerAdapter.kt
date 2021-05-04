package org.cru.godtools.tract.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.ccci.gto.android.common.viewpager.adapter.DataBindingPagerAdapter
import org.ccci.gto.android.common.viewpager.adapter.DataBindingViewHolder
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.tract.ui.controller.PageController
import org.cru.godtools.tract.ui.controller.bindController
import org.cru.godtools.xml.model.Event
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.tips.Tip
import org.cru.godtools.xml.model.tract.Card
import org.cru.godtools.xml.model.tract.Modal
import org.cru.godtools.xml.model.tract.TractPage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ManifestPagerAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    private val pageControllerFactory: PageController.Factory,
    eventBus: EventBus
) : DataBindingPagerAdapter<TractPageBinding>(lifecycleOwner), PageController.Callbacks, Observer<Manifest?> {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner): ManifestPagerAdapter
    }

    interface Callbacks {
        fun onUpdateActiveCard(page: TractPage, card: Card?)
        fun showModal(modal: Modal)
        fun showTip(tip: Tip)
        fun goToPage(position: Int)
    }

    internal var manifest: Manifest? = null
        set(value) {
            val changed = field !== value
            field = value
            if (changed) notifyDataSetChanged()
        }
    var callbacks: Callbacks? = null
    var showTips: Boolean = false

    init {
        setHasStableIds(true)
        eventBus.register(lifecycleOwner, this)
    }

    override fun getCount() = manifest?.tractPages?.size ?: 0
    private fun getItem(position: Int) = manifest?.tractPages?.getOrNull(position)
    override fun getItemId(position: Int) = getItem(position)?.id?.let { IdUtils.convertId(it) } ?: NO_ID
    override fun getItemPositionFromId(id: Long) =
        manifest?.tractPages?.indexOfFirst { id == IdUtils.convertId(it.id) } ?: PagerAdapter.POSITION_NONE

    private val primaryItemController get() = primaryItemBinding?.controller
    private val primaryItemPage get() = primaryItemController?.model

    // region Lifecycle
    override fun onChanged(t: Manifest?) {
        manifest = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup) =
        TractPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onViewDataBindingCreated(binding: TractPageBinding) {
        binding.bindController(pageControllerFactory, lifecycleOwner).also {
            it.callbacks = this
            it.isTipsEnabled = showTips
        }
    }

    override fun onBindViewDataBinding(
        holder: DataBindingViewHolder<TractPageBinding>,
        binding: TractPageBinding,
        position: Int
    ) {
        binding.controller?.let { controller ->
            controller.model = getItem(position)
            controller.lifecycleOwner?.apply { maxState = maxOf(maxState, Lifecycle.State.STARTED) }
        }
    }

    override fun onViewDataBindingRecycled(holder: DataBindingViewHolder<TractPageBinding>, binding: TractPageBinding) {
        binding.controller?.lifecycleOwner?.maxState = Lifecycle.State.CREATED
    }

    override fun onUpdatePrimaryItem(
        oldHolder: DataBindingViewHolder<TractPageBinding>?,
        oldBinding: TractPageBinding?,
        holder: DataBindingViewHolder<TractPageBinding>?,
        binding: TractPageBinding?
    ) {
        val controller = binding?.controller
        controller?.model?.let { callbacks?.onUpdateActiveCard(it, controller.activeCard) }

        val oldController = oldBinding?.controller
        if (oldController !== controller) {
            oldController?.lifecycleOwner?.maxState = Lifecycle.State.STARTED
            controller?.lifecycleOwner?.maxState = Lifecycle.State.RESUMED
        }
    }

    fun onContentEvent(event: Event) {
        primaryItemController?.onContentEvent(event)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onLiveShareNavigationEvent(event: NavigationEvent) {
        primaryItemController?.onLiveShareNavigationEvent(event)
    }
    // endregion Lifecycle

    // region PageController.Callbacks
    override fun onUpdateActiveCard(page: TractPage?, card: Card?) {
        if (page == null) return
        if (primaryItemPage != page) return

        callbacks?.onUpdateActiveCard(page, card)
    }

    override fun showModal(modal: Modal) {
        if (primaryItemPage == modal.page) callbacks?.showModal(modal)
    }

    override fun showTip(tip: Tip) {
        callbacks?.showTip(tip)
    }

    override fun goToNextPage() {
        callbacks?.goToPage((primaryItemPage?.position ?: -1) + 1)
    }
    // endregion PageController.Callbacks
}
