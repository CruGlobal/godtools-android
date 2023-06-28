package org.cru.godtools.activity

import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.androidx.drawerlayout.widget.toggleDrawer
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES
import org.cru.godtools.R
import org.cru.godtools.base.ui.activity.BaseBindingActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ActivityGenericFragmentWithNavDrawerBinding
import org.cru.godtools.ui.databinding.ActivityGenericFragmentBinding
import org.cru.godtools.ui.drawer.DrawerContentLayout
import org.cru.godtools.ui.languages.startLanguageSettingsActivity

abstract class BasePlatformActivity<B : ViewBinding> protected constructor(@LayoutRes contentLayoutId: Int) :
    BaseBindingActivity<B>(contentLayoutId) {
    protected constructor() : this(INVALID_LAYOUT_RES)

    // region Lifecycle
    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()
        setupNavigationDrawer()
    }

    @CallSuper
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        if (drawerLayout != null) {
            supportActionBar?.setHomeButtonEnabled(true)
        }
    }

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

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
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
                    dismissDrawer = { closeNavigationDrawer() },
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
}
