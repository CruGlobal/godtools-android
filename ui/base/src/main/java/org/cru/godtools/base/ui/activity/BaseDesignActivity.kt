package org.cru.godtools.base.ui.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.ccci.gto.android.common.compat.view.ViewCompat
import org.cru.godtools.base.ui.R2

private const val EXTRA_NAV_TAB_ACTIVE = "org.cru.godtools.base.ui.activity.BaseDesignActivity.EXTRA_NAV_TAB_SELECTED"

abstract class BaseDesignActivity<B : ViewBinding>(@LayoutRes contentLayoutId: Int) : BaseActivity<B>(contentLayoutId),
    OnTabSelectedListener {
    // region Lifecycle
    @CallSuper
    override fun onContentChanged() {
        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        BaseDesignActivity_ViewBinding(this)

        super.onContentChanged()
        setupNavigationTabs()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreNavigationTabsState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveNavigationTabsState(outState)
    }
    // endregion Lifecycle

    // region AppBar Tabs
    @JvmField
    @BindView(R2.id.appbar_tabs)
    var navigationTabs: TabLayout? = null

    @CallSuper
    protected open fun setupNavigationTabs() {
        navigationTabs?.apply {
            ViewCompat.setClipToOutline(this, true)
            addOnTabSelectedListener(this@BaseDesignActivity)

            // disable the action bar title since we have navigation tabs
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    private fun restoreNavigationTabsState(savedState: Bundle) {
        navigationTabs?.run {
            removeOnTabSelectedListener(this@BaseDesignActivity)
            selectNavigationTabIfNecessary(getTabAt(savedState.getInt(EXTRA_NAV_TAB_ACTIVE, -1)))
            addOnTabSelectedListener(this@BaseDesignActivity)
        }
    }

    private fun saveNavigationTabsState(savedState: Bundle) {
        navigationTabs?.let { savedState.putInt(EXTRA_NAV_TAB_ACTIVE, it.selectedTabPosition) }
    }

    protected fun selectNavigationTabIfNecessary(tab: TabLayout.Tab?) {
        tab?.takeUnless { it.isSelected }?.select()
    }

    override fun onTabSelected(tab: TabLayout.Tab) = Unit
    override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    override fun onTabReselected(tab: TabLayout.Tab) = Unit
    // endregion AppBar Tabs
}
