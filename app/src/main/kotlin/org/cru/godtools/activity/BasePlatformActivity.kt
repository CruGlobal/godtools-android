package org.cru.godtools.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.navigation.NavigationView
import dagger.Lazy
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.drawerlayout.widget.toggleDrawer
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.ccci.gto.android.common.util.view.MenuUtils
import org.cru.godtools.R
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.URI_SHARE_BASE
import org.cru.godtools.base.ui.activity.BaseBindingActivity
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.ActivityGenericFragmentWithNavDrawerBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.shared.analytics.AnalyticsActionNames
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.startTutorialActivity
import org.cru.godtools.ui.about.startAboutActivity
import org.cru.godtools.ui.account.startAccountActivity
import org.cru.godtools.ui.databinding.ActivityGenericFragmentBinding
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

internal val MAILTO_SUPPORT = Uri.parse("mailto:support@godtoolsapp.com")
internal val URI_SUPPORT = Uri.parse("https://godtoolsapp.com/#contact")
internal val URI_HELP = Uri.parse("https://godtoolsapp.com/faq/")
internal val URI_PRIVACY = Uri.parse("https://www.cru.org/about/privacy.html")
internal val URI_TERMS_OF_USE = Uri.parse("https://godtoolsapp.com/terms-of-use/")
internal val URI_COPYRIGHT = Uri.parse("https://godtoolsapp.com/copyright/")

private const val EXTRA_SYNC_HELPER = "org.cru.godtools.activity.BasePlatformActivity.SYNC_HELPER"

abstract class BasePlatformActivity<B : ViewBinding> protected constructor(@LayoutRes contentLayoutId: Int) :
    BaseBindingActivity<B>(contentLayoutId), NavigationView.OnNavigationItemSelectedListener {
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

    @CallSuper
    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_about -> {
            startAboutActivity()
            true
        }
        R.id.action_profile -> {
            startAccountActivity()
            true
        }
        R.id.action_login, R.id.action_signup -> {
            launchLogin()
            true
        }
        R.id.action_logout -> {
            lifecycleScope.launch { accountManager.logout() }
            true
        }
        R.id.action_help -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_HELP, deviceLocale))
            openUrl(URI_HELP)
            true
        }
        R.id.action_share -> {
            launchShare()
            true
        }
        R.id.action_share_story -> {
            launchShareStory()
            true
        }
        R.id.action_contact_us -> {
            launchContactUs()
            true
        }
        R.id.action_tutorial -> {
            launchTrainingTutorial()
            true
        }
        R.id.action_terms_of_use -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_TERMS_OF_USE, deviceLocale))
            openUrl(URI_TERMS_OF_USE)
            true
        }
        R.id.action_privacy_policy -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_PRIVACY_POLICY, deviceLocale))
            openUrl(URI_PRIVACY)
            true
        }
        R.id.action_copyright -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_COPYRIGHT, deviceLocale))
            openUrl(URI_COPYRIGHT)
            true
        }
        else -> onOptionsItemSelected(item)
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
    protected open val drawerMenu: NavigationView? get() = findViewById(R.id.drawer_menu)
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
        drawerMenu?.apply {
            setNavigationItemSelectedListener { item ->
                onNavigationItemSelected(item)
                    .also { selected -> if (selected) closeNavigationDrawer() }
            }

            with(menu) {
                // the tutorial menu item is currently only available in English
                findItem(R.id.action_tutorial)?.isVisible = PageSet.FEATURES.supportsLocale(deviceLocale)

                // login items visibility
                if (showLoginItems) {
                    val loginItem = findItem(R.id.action_login)
                    val signupItem = findItem(R.id.action_signup)
                    val logoutItem = findItem(R.id.action_logout)
                    val profileItem = findItem(R.id.action_profile)

                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            accountManager.isAuthenticatedFlow().collect { isAuthenticated ->
                                loginItem?.isVisible = !isAuthenticated
                                signupItem?.isVisible = !isAuthenticated
                                logoutItem?.isVisible = isAuthenticated
                                profileItem?.isVisible = isAuthenticated
                            }
                        }
                    }
                } else {
                    // hide all menu items if we aren't showing login items for this language
                    MenuUtils.setGroupVisibleRecursively(this, R.id.group_login_items, false)
                }
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

    private fun launchContactUs() {
        eventBus.post(AnalyticsScreenEvent(AnalyticsActionNames.PLATFORM_CONTACT_US, deviceLocale))
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT))
        } catch (e: ActivityNotFoundException) {
            openUrl(URI_SUPPORT)
        }
    }

    private fun launchShare() {
        eventBus.post(AnalyticsScreenEvent(AnalyticsActionNames.PLATFORM_SHARE_GODTOOLS, settings.primaryLanguage))
        val shareLink = URI_SHARE_BASE.buildUpon()
            .appendPath(settings.primaryLanguage.toLanguageTag().lowercase(Locale.US))
            .appendPath("")
            .build().toString()

        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, getString(org.cru.godtools.ui.R.string.app_name))
            .putExtra(Intent.EXTRA_TEXT, getString(org.cru.godtools.ui.R.string.share_tool_message, shareLink))
            .let { Intent.createChooser(it, null) }
            .also { startActivity(it) }
    }

    private fun launchShareStory() {
        eventBus.post(AnalyticsScreenEvent(AnalyticsActionNames.PLATFORM_SHARE_STORY, deviceLocale))
        try {
            startActivity(
                Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT)
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_story_subject))
            )
        } catch (e: ActivityNotFoundException) {
            openUrl(URI_SUPPORT)
        }
    }

    private fun launchTrainingTutorial() = startTutorialActivity(PageSet.FEATURES)
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
        if (handleChildrenSyncs) supportFragmentManager.fragments.filterIsInstance<BasePlatformFragment<*>>()
            .forEach { with(it) { triggerSync(force) } }
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
