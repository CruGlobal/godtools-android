package org.keynote.godtools.android.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.Settings;
import org.keynote.godtools.android.util.WebUrlLauncher;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.ccci.gto.android.common.Constants.INVALID_STRING_RES;
import static org.keynote.godtools.android.Constants.URI_HELP;
import static org.keynote.godtools.android.Constants.URI_SHARE_BASE;
import static org.keynote.godtools.android.utils.Constants.SHARE_LINK;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // App/Action Bar
    @Nullable
    @BindView(R.id.appBar)
    Toolbar mToolbar;
    @Nullable
    private ActionBar mActionBar;

    // Navigation Drawer
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Nullable
    @BindView(R.id.drawer_menu)
    NavigationView mDrawerMenu;
    @Nullable
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mVisible = false;

    /* BEGIN lifecycle */

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
        setupActionBar();
        setupNavigationDrawer();
    }

    protected void onSetupActionBar(@NonNull final ActionBar actionBar) {}

    @Override
    protected void onStart() {
        super.onStart();
        mVisible = true;
    }

    protected void onUpdateActionBar(@NonNull final ActionBar actionBar) {}

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // handle drawer navigation toggle
                if (mDrawerLayout != null && mDrawerToggle != null) {
                    if (mDrawerToggle.isDrawerIndicatorEnabled()) {
                        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            mDrawerLayout.openDrawer(GravityCompat.START);
                        }
                        return true;
                    }
                }
                break;
            case R.id.action_switch_language:
                LanguageSettingsActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @CallSuper
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                WebUrlLauncher.openUrl(this, URI_HELP);
                return true;
            case R.id.action_rate:
                openPlayStore();
                return true;
            case R.id.action_share:
                launchShare();
                return true;
        }

        return onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVisible = false;
    }

    /* END lifecycle */

    @NonNull
    protected Settings prefs() {
        return Settings.getInstance(this);
    }

    private void setupActionBar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            if (mDrawerLayout != null) {
                mActionBar.setHomeButtonEnabled(true);
            }
            onSetupActionBar(mActionBar);
        }
        updateActionBar();
    }

    protected final void updateActionBar() {
        final ActionBar actionBar = mActionBar;
        if (actionBar != null) {
            onUpdateActionBar(actionBar);
        }
    }

    private void setupNavigationDrawer() {
        if (mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, INVALID_STRING_RES, INVALID_STRING_RES);
            mDrawerToggle.setDrawerIndicatorEnabled(showNavigationDrawerIndicator());
            mDrawerToggle.setDrawerSlideAnimationEnabled(false);
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }
        if (mDrawerMenu != null) {
            mDrawerMenu.setNavigationItemSelectedListener(item -> {
                final boolean handled = onNavigationItemSelected(item);
                if (handled) {
                    closeNavigationDrawer();
                }
                return handled;
            });
        }
    }

    protected final void closeNavigationDrawer() {
        closeNavigationDrawer(mVisible);
    }

    protected final void closeNavigationDrawer(final boolean animate) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START, animate);
        }
    }

    protected boolean showNavigationDrawerIndicator() {return false;}

    private void openPlayStore() {
        final String appId = BuildConfig.APPLICATION_ID;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                                     Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
        }
    }

    private void launchShare() {
        final String text = getString(R.string.share_general_message)
                .replace(SHARE_LINK, URI_SHARE_BASE + prefs().getPrimaryLanguage());

        final Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));
    }
}
