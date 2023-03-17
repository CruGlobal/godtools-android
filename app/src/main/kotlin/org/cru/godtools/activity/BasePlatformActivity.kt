package org.cru.godtools.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import dagger.Lazy
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.drawerlayout.widget.toggleDrawer
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.base.ui.activity.BaseBindingActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ActivityGenericFragmentWithNavDrawerBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.databinding.ActivityGenericFragmentBinding
import org.cru.godtools.ui.drawer.DrawerContentLayout
import org.cru.godtools.ui.drawer.DrawerMenuEvent
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val EXTRA_SYNC_HELPER = "org.cru.godtools.activity.BasePlatformActivity.SYNC_HELPER"

abstract class BasePlatformActivity<B : ViewBinding> protected constructor(@LayoutRes contentLayoutId: Int) :
    BaseBindingActivity<B>(contentLayoutId) {
    protected constructor() : this(INVALID_LAYOUT_RES)

    @Inject
    internal lateinit var accountManager: GodToolsAccountManager

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restore any saved state
        savedInstanceState?.restoreSyncState()
    }

    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()
        setupNavigationDrawer()
        setupSyncUi()
    }

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
        startSyncFramework()
    }

    @CallSuper
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        if (drawerLayout != null) {
            supportActionBar?.setHomeButtonEnabled(true)
        }
    }

    @CallSuper
    protected open fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) = Unit

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            val layout = drawerLayout?.takeIf { drawerToggle?.isDrawerIndicatorEnabled == true }
            if (layout != null) {
                layout.toggleDrawer(GravityCompat.START)
                true
            } else {
                super.onOptionsItemSelected(item)
            }
        }
        R.id.action_switch_language -> {
            startLanguageSettingsActivity()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = when {
        closeNavigationDrawer() -> Unit
        else -> super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.saveSyncState()
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
        stopSyncFramework()
    }

    override fun onDestroy() {
        cleanupSyncUi()
        super.onDestroy()
    }
    // endregion Lifecycle

    override val toolbar get() = when (val it = binding) {
        is ActivityGenericFragmentBinding -> it.appbar
        is ActivityGenericFragmentWithNavDrawerBinding -> it.genericActivity.appbar
        else -> super.toolbar
    }

    // region Navigation Drawer
    protected open val drawerLayout: DrawerLayout? get() = findViewById(R.id.drawer_layout)
    protected open val drawerMenu: ComposeView? get() = findViewById(R.id.drawer_menu)
    private var drawerToggle: ActionBarDrawerToggle? = null

    private val showLoginItems by lazy { resources.getBoolean(R.bool.show_login_menu_items) }
    protected open val isShowNavigationDrawerIndicator: LiveData<Boolean> = ImmutableLiveData(false)

    private fun setupNavigationDrawer() {
        drawerLayout?.let { layout ->
            drawerToggle = ActionBarDrawerToggle(this, layout, INVALID_STRING_RES, INVALID_STRING_RES)
                .apply { isDrawerSlideAnimationEnabled = false }
                .also { toggle ->
                    isShowNavigationDrawerIndicator.observe(this) { toggle.isDrawerIndicatorEnabled = it }
                    layout.addDrawerListener(toggle)
                    toggle.syncState()
                }
        }
        drawerMenu?.setContent {
            GodToolsTheme {
                DrawerContentLayout(
                    onEvent = {
                        when (it) {
                            DrawerMenuEvent.LOGIN, DrawerMenuEvent.SIGNUP -> launchLogin()
                        }
                        closeNavigationDrawer()
                    }
                )
            }
        }
    }

    /**
     * @return true if the navigation drawer was closed, false otherwise
     */
    private fun closeNavigationDrawer(): Boolean {
        drawerLayout?.takeIf { it.isDrawerOpen(GravityCompat.START) }?.apply {
            closeDrawer(GravityCompat.START, lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            return true
        }

        return false
    }
    // endregion Navigation Drawer

    // region Navigation Menu actions
    private fun launchLogin() {
        lifecycleScope.launch { accountManager.login(this@BasePlatformActivity, AccountType.OKTA) }
    }
    // endregion Navigation Menu actions

    // region Sync Logic
    protected open val swipeRefreshLayout: SwipeRefreshLayout? get() = null
    open val handleChildrenSyncs get() = swipeRefreshLayout != null

    @Inject
    internal lateinit var lazySyncService: Lazy<GodToolsSyncService>
    protected val syncService: GodToolsSyncService get() = lazySyncService.get()

    private val syncHelper = SwipeRefreshSyncHelper()
    private val syncFragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
            if (f is BasePlatformFragment<*> && handleChildrenSyncs) with(f) { syncHelper.triggerSync() }
        }
    }

    private fun startSyncFramework() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(syncFragmentCallbacks, false)
        syncHelper.triggerSync()
    }

    private fun SwipeRefreshSyncHelper.triggerSync(force: Boolean = false) {
        onSyncData(this, force)
        if (handleChildrenSyncs) {
            supportFragmentManager.fragments.filterIsInstance<BasePlatformFragment<*>>()
                .forEach { with(it) { triggerSync(force) } }
        }
        updateState()
    }

    private fun stopSyncFramework() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(syncFragmentCallbacks)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    internal fun syncCompleted(event: SyncFinishedEvent) = syncHelper.updateState()

    private fun Bundle.restoreSyncState() {
        syncHelper.onRestoreInstanceState(getBundle(EXTRA_SYNC_HELPER))
    }

    private fun setupSyncUi() {
        syncHelper.refreshLayout = swipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener { syncHelper.triggerSync(true) }
    }

    private fun cleanupSyncUi() {
        swipeRefreshLayout?.setOnRefreshListener(null)
        syncHelper.refreshLayout = null
    }

    private fun Bundle.saveSyncState() {
        putBundle(EXTRA_SYNC_HELPER, syncHelper.onSaveInstanceState())
    }
    // endregion Sync Logic
}
