package org.cru.godtools.base.ui.activity;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;

import org.ccci.gto.android.common.compat.view.ViewCompat;
import org.cru.godtools.base.ui.R2;

import butterknife.BindView;

public abstract class BaseDesignActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
    private static final String EXTRA_NAV_TAB_ACTIVE = BaseDesignActivity.class.getName() + ".EXTRA_NAV_TAB_SELECTED";

    // Navigation Tabs
    @Nullable
    @BindView(R2.id.appbar_tabs)
    protected TabLayout mNavigationTabs;

    // region Lifecycle Events

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        new BaseDesignActivity_ViewBinding(this);

        setupNavigationTabs();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            restoreNavigationTabsState(savedInstanceState);
        }
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {}

    @Override
    public void onTabUnselected(final TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(final TabLayout.Tab tab) {}

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveNavigationTabsState(outState);
    }

    // endregion Lifecycle Events

    @CallSuper
    protected void setupNavigationTabs() {
        if (mNavigationTabs != null) {
            ViewCompat.setClipToOutline(mNavigationTabs, true);
            mNavigationTabs.addOnTabSelectedListener(this);

            // disable the action bar title since we have navigation tabs
            if (mActionBar != null) {
                mActionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void restoreNavigationTabsState(@NonNull final Bundle savedState) {
        if (mNavigationTabs != null) {
            mNavigationTabs.removeOnTabSelectedListener(this);
            final TabLayout.Tab activeTab = mNavigationTabs.getTabAt(savedState.getInt(EXTRA_NAV_TAB_ACTIVE, -1));
            selectNavigationTabIfNecessary(activeTab);
            mNavigationTabs.addOnTabSelectedListener(this);
        }
    }

    private void saveNavigationTabsState(@NonNull final Bundle savedState) {
        if (mNavigationTabs != null) {
            savedState.putInt(EXTRA_NAV_TAB_ACTIVE, mNavigationTabs.getSelectedTabPosition());
        }
    }

    protected void selectNavigationTabIfNecessary(@Nullable final TabLayout.Tab tab) {
        if (tab != null && !tab.isSelected()) {
            tab.select();
        }
    }
}
