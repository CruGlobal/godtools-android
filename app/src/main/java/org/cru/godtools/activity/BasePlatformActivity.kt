package org.cru.godtools.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.observe
import butterknife.BindView
import com.google.android.material.navigation.NavigationView
import me.thekey.android.TheKey
import me.thekey.android.livedata.defaultSessionGuidLiveData
import me.thekey.android.view.dialog.LoginDialogFragment
import org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.util.content.ComponentNameUtils
import org.ccci.gto.android.common.util.view.MenuUtils
import org.cru.godtools.BuildConfig
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_CONTACT_US
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_COPYRIGHT
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_HELP
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_PRIVACY_POLICY
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_SHARE_GODTOOLS
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_SHARE_STORY
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_TERMS_OF_USE
import org.cru.godtools.base.Constants.URI_SHARE_BASE
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.PREF_PARALLEL_LANGUAGE
import org.cru.godtools.base.Settings.Companion.PREF_PRIMARY_LANGUAGE
import org.cru.godtools.base.ui.activity.BaseDesignActivity
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.ui.about.startAboutActivity
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.keynote.godtools.android.activity.MainActivity
import java.util.Locale

internal val MAILTO_SUPPORT = Uri.parse("mailto:support@godtoolsapp.com")
internal val URI_SUPPORT = Uri.parse("https://godtoolsapp.com/#contact")
internal val URI_HELP = Uri.parse("https://godtoolsapp.com/faq/")
internal val URI_PRIVACY = Uri.parse("https://www.cru.org/about/privacy.html")
internal val URI_TERMS_OF_USE = Uri.parse("https://godtoolsapp.com/terms-of-use/")
internal val URI_COPYRIGHT = Uri.parse("https://godtoolsapp.com/copyright/")

private const val SHARE_LINK = "{{share_link}}"

private const val TAG_KEY_LOGIN_DIALOG = "keyLoginDialog"

abstract class BasePlatformActivity : BaseDesignActivity(), NavigationView.OnNavigationItemSelectedListener {
    protected val settings by lazy { Settings.getInstance(this) }
    private val settingsChangeListener = OnSharedPreferenceChangeListener { prefs, k -> onSettingsUpdated(prefs, k) }
    protected val theKey by lazy { TheKey.getInstance(this) }

    private var primaryLanguage = Settings.defaultLanguage
    private var parallelLanguage: Locale? = null

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLanguages(true)
    }

    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()
        setupNavigationDrawer()
    }

    @CallSuper
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        if (drawerLayout != null) {
            mActionBar?.setHomeButtonEnabled(true)
        }
    }

    override fun onStart() {
        super.onStart()
        startSettingsChangeListener()
        loadLanguages(false)
    }

    @CallSuper
    fun onSettingsUpdated(preferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_PRIMARY_LANGUAGE, PREF_PARALLEL_LANGUAGE -> loadLanguages(false)
        }
    }

    protected fun onUpdatePrimaryLanguage() = Unit
    protected fun onUpdateParallelLanguage() = Unit

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                drawerLayout
                    ?.takeIf { drawerToggle?.isDrawerIndicatorEnabled ?: false }
                    ?.apply {
                        // handle drawer navigation toggle
                        when {
                            isDrawerVisible(GravityCompat.START) -> closeDrawer(GravityCompat.START)
                            else -> openDrawer(GravityCompat.START)
                        }
                        return true
                    }
            R.id.action_switch_language -> {
                startLanguageSettingsActivity()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @CallSuper
    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_about -> {
            startAboutActivity()
            true
        }
        R.id.action_login -> {
            launchLogin(false)
            true
        }
        R.id.action_signup -> {
            launchLogin(true)
            true
        }
        R.id.action_logout -> {
            theKey.logout()
            true
        }
        R.id.action_help -> {
            mEventBus.post(AnalyticsScreenEvent(SCREEN_HELP, deviceLocale))
            openUrl(URI_HELP)
            true
        }
        R.id.action_rate -> {
            openPlayStore()
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
            mEventBus.post(AnalyticsScreenEvent(SCREEN_TERMS_OF_USE, deviceLocale))
            openUrl(URI_TERMS_OF_USE)
            true
        }
        R.id.action_privacy_policy -> {
            mEventBus.post(AnalyticsScreenEvent(SCREEN_PRIVACY_POLICY, deviceLocale))
            openUrl(URI_PRIVACY)
            true
        }
        R.id.action_copyright -> {
            mEventBus.post(AnalyticsScreenEvent(SCREEN_COPYRIGHT, deviceLocale))
            openUrl(URI_COPYRIGHT)
            true
        }
        else -> onOptionsItemSelected(item)
    }

    override fun onBackPressed() = when {
        closeNavigationDrawer() -> Unit
        else -> super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        mEventBus.unregister(this)
        stopSettingsChangeListener()
    }
    // endregion Lifecycle

    // region Navigation Drawer
    @JvmField
    @BindView(R.id.drawer_layout)
    protected var drawerLayout: DrawerLayout? = null
    @JvmField
    @BindView(R.id.drawer_menu)
    internal var drawerMenu: NavigationView? = null
    private var drawerToggle: ActionBarDrawerToggle? = null

    private val showLoginItems by lazy { resources.getBoolean(R.bool.show_login_menu_items) }
    protected open val isShowNavigationDrawerIndicator get() = false

    private fun setupNavigationDrawer() {
        drawerLayout?.let {
            drawerToggle = ActionBarDrawerToggle(this, it, INVALID_STRING_RES, INVALID_STRING_RES)
                .apply {
                    isDrawerIndicatorEnabled = isShowNavigationDrawerIndicator
                    isDrawerSlideAnimationEnabled = false
                }
                .also { toggle ->
                    it.addDrawerListener(toggle)
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
                findItem(R.id.action_tutorial)?.isVisible = deviceLocale.language == Locale.ENGLISH.language

                // login items visibility
                if (showLoginItems) {
                    val loginItem = findItem(R.id.action_login)
                    val signupItem = findItem(R.id.action_signup)
                    val logoutItem = findItem(R.id.action_logout)
                    theKey.defaultSessionGuidLiveData.observe(this@BasePlatformActivity) { guid ->
                        loginItem?.isVisible = guid == null
                        signupItem?.isVisible = guid == null
                        logoutItem?.isVisible = guid != null
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

    private fun loadLanguages(initial: Boolean) {
        val oldPrimary = primaryLanguage
        val oldParallel = parallelLanguage
        primaryLanguage = settings.primaryLanguage
        parallelLanguage = settings.parallelLanguage

        // trigger lifecycle events
        if (!initial) {
            if (oldPrimary != primaryLanguage) onUpdatePrimaryLanguage()
            if (oldParallel != parallelLanguage) onUpdateParallelLanguage()
        }
    }

    private fun startSettingsChangeListener() =
        settings.registerOnSharedPreferenceChangeListener(settingsChangeListener)

    private fun stopSettingsChangeListener() =
        settings.unregisterOnSharedPreferenceChangeListener(settingsChangeListener)

    // region Navigation Menu actions
    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
        } catch (e: ActivityNotFoundException) {
            openUrl(Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"))
        }
    }

    private fun launchLogin(signup: Boolean) {
        val redirectUri = Uri.Builder()
            .scheme("https")
            .authority(getString(R.string.account_deeplink_host))
            .path(getString(R.string.account_deeplink_path))
            .build()

        // try using an external browser first if we will deeplink back to GodTools
        var handled = false
        if (ComponentNameUtils.isDefaultComponentFor(this, MainActivity::class.java, redirectUri)) {
            handled = openUrl(
                theKey.loginUriBuilder()
                    .redirectUri(redirectUri)
                    .signup(signup)
                    .build()
            )
        }

        // fallback to an in-app DialogFragment for login
        if (!handled) {
            val fm = supportFragmentManager
            if (fm.findFragmentByTag(TAG_KEY_LOGIN_DIALOG) == null) {
                val loginDialogFragment = LoginDialogFragment.builder()
                    .redirectUri(redirectUri)
                    .signup(signup)
                    .build()
                loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), TAG_KEY_LOGIN_DIALOG)
            }
        }
    }

    private fun launchContactUs() {
        mEventBus.post(AnalyticsScreenEvent(SCREEN_CONTACT_US, deviceLocale))
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT))
        } catch (e: ActivityNotFoundException) {
            openUrl(URI_SUPPORT)
        }
    }

    private fun launchShare() {
        mEventBus.post(AnalyticsScreenEvent(SCREEN_SHARE_GODTOOLS, primaryLanguage))
        val shareLink = URI_SHARE_BASE.buildUpon()
            .appendPath(LocaleCompat.toLanguageTag(primaryLanguage).toLowerCase())
            .appendPath("")
            .build().toString()
        val text = getString(R.string.share_general_message)
            .replace(SHARE_LINK, shareLink)

        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            .putExtra(Intent.EXTRA_TEXT, text)
            .let { Intent.createChooser(it, getString(R.string.share_prompt)) }
            .also { startActivity(it) }
    }

    private fun launchShareStory() {
        mEventBus.post(AnalyticsScreenEvent(SCREEN_SHARE_STORY, deviceLocale))
        try {
            startActivity(
                Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT)
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_story_subject))
            )
        } catch (e: ActivityNotFoundException) {
            openUrl(URI_SUPPORT)
        }
    }

    private fun launchTrainingTutorial() = startTutorialActivity(PageSet.TRAINING)
    // endregion Navigation Menu actions
}
