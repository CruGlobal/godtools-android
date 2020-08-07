package org.cru.godtools.tract.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.onStart
import org.ccci.gto.android.common.androidx.lifecycle.onStop
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.ccci.gto.android.common.viewpager.adapter.DataBindingPagerAdapter
import org.ccci.gto.android.common.viewpager.adapter.DataBindingViewHolder
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.tract.ui.controller.PageController
import org.cru.godtools.tract.ui.controller.bindController
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Modal
import org.cru.godtools.xml.model.Page
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ManifestPagerAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    private val pageControllerFactory: PageController.Factory,
    eventBus: EventBus
) : DataBindingPagerAdapter<TractPageBinding>(lifecycleOwner), PageController.Callbacks, Observer<Manifest?> {
    @AssistedInject.Factory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner): ManifestPagerAdapter
    }

    interface Callbacks {
        fun onUpdateActiveCard(page: Page, card: Card?)
        fun showModal(modal: Modal)
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
        lifecycleOwner.lifecycle.onStart { primaryItem?.binding?.controller?.isVisible = true }
        lifecycleOwner.lifecycle.onStop { primaryItem?.binding?.controller?.isVisible = false }
    }

    override fun getCount() = manifest?.pages?.size ?: 0
    private fun getItem(position: Int) = manifest?.pages?.getOrNull(position)
    override fun getItemId(position: Int) = getItem(position)?.id?.let { IdUtils.convertId(it) } ?: NO_ID
    override fun getItemPositionFromId(id: Long) =
        manifest?.pages?.indexOfFirst { id == IdUtils.convertId(it.id) } ?: PagerAdapter.POSITION_NONE

    private val primaryItemController get() = primaryItemBinding?.controller
    private val primaryItemPage get() = primaryItemController?.model

    // region Lifecycle
    override fun onChanged(t: Manifest?) {
        manifest = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup) =
        TractPageBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            bindController(pageControllerFactory).also {
                it.callbacks = this@ManifestPagerAdapter
                it.isTipsEnabled = showTips
            }
        }

    override fun onBindViewDataBinding(
        holder: DataBindingViewHolder<TractPageBinding>,
        binding: TractPageBinding,
        position: Int
    ) {
        binding.controller?.model = getItem(position)
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
            oldController?.isVisible = false
            controller?.isVisible = true
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
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
    override fun onUpdateActiveCard(page: Page?, card: Card?) {
        if (page == null) return
        if (primaryItemPage != page) return

        callbacks?.onUpdateActiveCard(page, card)
    }

    override fun showModal(modal: Modal) {
        if (primaryItemPage == modal.page) callbacks?.showModal(modal)
    }

    override fun goToNextPage() {
        callbacks?.goToPage((primaryItemPage?.position ?: -1) + 1)
    }
    // endregion PageController.Callbacks
}
