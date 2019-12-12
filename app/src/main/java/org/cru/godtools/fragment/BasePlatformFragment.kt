package org.cru.godtools.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import org.ccci.gto.android.sync.event.SyncFinishedEvent
import org.ccci.gto.android.sync.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.PREF_FEATURE_DISCOVERED
import org.cru.godtools.base.Settings.PREF_PARALLEL_LANGUAGE
import org.cru.godtools.base.Settings.PREF_PRIMARY_LANGUAGE
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale

private const val EXTRA_SYNC_HELPER = "org.cru.godtools.fragment.BasePlatformFragment.SYNC_HELPER"

abstract class BasePlatformFragment : BaseFragment() {
    protected lateinit var settings: Settings
    private val settingsChangeListener = ChangeListener()

    @JvmField
    @BindView(R.id.refresh)
    internal var refreshLayout: SwipeRefreshLayout? = null
    protected val syncHelper = SwipeRefreshSyncHelper()

    protected var primaryLanguage = Settings.getDefaultLanguage()
    protected var parallelLanguage: Locale? = null

    // region Lifecycle Events

    override fun onAttach(context: Context) {
        super.onAttach(context)
        settings = Settings.getInstance(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restore any saved state
        savedInstanceState?.let {
            syncHelper.onRestoreInstanceState(it.getBundle(EXTRA_SYNC_HELPER))
        }

        loadLanguages(true)
        syncData(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefreshView()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        startLanguagesChangeListener()
        loadLanguages(false)
        syncHelper.updateState()
    }

    protected open fun onUpdatePrimaryLanguage() {}

    protected open fun onUpdateParallelLanguage() {}

    protected open fun onUpdateFeatureDiscovered() {}

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncCompleted(event: SyncFinishedEvent) = syncHelper.updateState()

    override fun onStop() {
        super.onStop()
        stopLanguagesChangeListener()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(EXTRA_SYNC_HELPER, syncHelper.onSaveInstanceState())
    }

    override fun onDestroyView() {
        cleanupRefreshView()
        super.onDestroyView()
    }

    // endregion Lifecycle Events

    @CallSuper
    protected open fun syncData(force: Boolean) {}

    internal fun loadLanguages(initial: Boolean) {
        val oldPrimary = primaryLanguage
        primaryLanguage = settings.primaryLanguage
        val oldParallel = parallelLanguage
        parallelLanguage = settings.parallelLanguage

        // trigger lifecycle events
        if (!initial) {
            if (oldPrimary != primaryLanguage) {
                onUpdatePrimaryLanguage()
            }
            if (oldParallel != parallelLanguage) {
                onUpdateParallelLanguage()
            }
        }
    }

    private fun startLanguagesChangeListener() {
        settings.registerOnSharedPreferenceChangeListener(settingsChangeListener)
    }

    private fun stopLanguagesChangeListener() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsChangeListener)
    }

    private fun setupRefreshView() {
        syncHelper.setRefreshLayout(refreshLayout)
        refreshLayout?.setOnRefreshListener { syncData(true) }
    }

    private fun cleanupRefreshView() {
        syncHelper.setRefreshLayout(null)
        refreshLayout?.setOnRefreshListener(null)
    }

    internal inner class ChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
            when (key) {
                PREF_PRIMARY_LANGUAGE, PREF_PARALLEL_LANGUAGE -> loadLanguages(false)
            }
            if (key?.startsWith(PREF_FEATURE_DISCOVERED) == true) {
                onUpdateFeatureDiscovered()
            }
        }
    }
}
