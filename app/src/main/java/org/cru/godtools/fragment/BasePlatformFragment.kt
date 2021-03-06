package org.cru.godtools.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import javax.inject.Inject
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val EXTRA_SYNC_HELPER = "org.cru.godtools.fragment.BasePlatformFragment.SYNC_HELPER"

abstract class BasePlatformFragment<B : ViewDataBinding>(@LayoutRes layoutId: Int) : BaseFragment<B>(layoutId) {
    @Inject
    protected lateinit var eventBus: EventBus
    @Inject
    protected lateinit var settings: Settings

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restore any saved state
        savedInstanceState?.let {
            syncHelper.onRestoreInstanceState(it.getBundle(EXTRA_SYNC_HELPER))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        triggerInitialSync()
    }

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
        syncHelper.updateState()
    }

    @CallSuper
    protected open fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) = Unit

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncCompleted(event: SyncFinishedEvent) = syncHelper.updateState()

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(EXTRA_SYNC_HELPER, syncHelper.onSaveInstanceState())
    }

    override fun onDestroyView() {
        cleanupRefreshView()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region Sync Logic
    @Inject
    protected lateinit var syncService: GodToolsSyncService

    private val syncHelper = SwipeRefreshSyncHelper()

    private fun triggerInitialSync() {
        if ((activity as? BasePlatformActivity<*>)?.handleChildrenSyncs != true) syncHelper.triggerSync()
    }

    internal fun SwipeRefreshSyncHelper.triggerSync(force: Boolean = false) {
        onSyncData(this, force)
        updateState()
    }

    internal fun SwipeRefreshLayout.setupSwipeRefresh() {
        syncHelper.refreshLayout = this
        setOnRefreshListener { syncHelper.triggerSync(true) }
    }

    private fun cleanupRefreshView() {
        syncHelper.refreshLayout = null
    }
    // endregion Sync Logic
}
