package org.cru.godtools.activity;

import android.arch.lifecycle.Lifecycle;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.ConfigurationCompat;
import android.support.v4.os.LocaleListCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.util.content.ComponentNameUtils;
import org.cru.godtools.BuildConfig;
import org.cru.godtools.R;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.ui.activity.BaseDesignActivity;
import org.cru.godtools.base.ui.util.WebUrlLauncher;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.activity.MainActivity;

import java.util.Locale;

import butterknife.BindView;
import me.thekey.android.TheKey;
import me.thekey.android.eventbus.event.TheKeyEvent;
import me.thekey.android.view.dialog.LoginDialogFragment;

import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_CONTACT_US;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_COPYRIGHT;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_HELP;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_PRIVACY_POLICY;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_SHARE_GODTOOLS;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_SHARE_STORY;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_TERMS_OF_USE;
import static org.cru.godtools.base.Constants.URI_SHARE_BASE;
import static org.cru.godtools.base.Settings.PREF_PARALLEL_LANGUAGE;
import static org.cru.godtools.base.Settings.PREF_PRIMARY_LANGUAGE;
import static org.cru.godtools.base.util.LocaleUtils.getDeviceLocale;
import static org.keynote.godtools.android.Constants.MAILTO_SUPPORT;
import static org.keynote.godtools.android.Constants.URI_COPYRIGHT;
import static org.keynote.godtools.android.Constants.URI_HELP;
import static org.keynote.godtools.android.Constants.URI_PRIVACY;
import static org.keynote.godtools.android.Constants.URI_SUPPORT;
import static org.keynote.godtools.android.Constants.URI_TERMS_OF_USE;
import static org.keynote.godtools.android.utils.Constants.SHARE_LINK;

public abstract class BasePlatformActivity extends BaseDesignActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG_KEY_LOGIN_DIALOG = "keyLoginDialog";

    private final ChangeListener mSettingsChangeListener = new ChangeListener();

    // List of supported Login Languages
    private String[] mLoginLanguages = {
            Locale.ENGLISH.getLanguage()
    };

    // Navigation Drawer
    @Nullable
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;
    @Nullable
    private ActionBarDrawerToggle mDrawerToggle;
    @Nullable
    @BindView(R.id.drawer_menu)
    NavigationView mDrawerMenu;
    @Nullable
    MenuItem mLoginItem;
    @Nullable
    MenuItem mSignupItem;
    @Nullable
    MenuItem mLogoutItem;

    @NonNull
    protected /*final*/ TheKey mTheKey;

    @NonNull
    protected Locale mPrimaryLanguage = Settings.getDefaultLanguage();
    @Nullable
    protected Locale mParallelLanguage;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheKey = TheKey.getInstance(this);
        loadLanguages(true);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setupNavigationDrawer();
    }

    @CallSuper
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        if (mActionBar != null && mDrawerLayout != null) {
            mActionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLanguagesChangeListener();
        mEventBus.register(this);
        loadLanguages(false);
        updateNavigationDrawerMenu();
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTheKeyEvent(@NonNull final TheKeyEvent event) {
        updateNavigationDrawerMenu();
    }

    protected void onUpdatePrimaryLanguage() {
    }

    protected void onUpdateParallelLanguage() {
    }

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
                showLanguageSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @CallSuper
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AboutActivity.start(this);
                return true;
            case R.id.action_login:
                launchLogin(false);
                return true;
            case R.id.action_signup:
                launchLogin(true);
                return true;
            case R.id.action_logout:
                mTheKey.logout();
                return true;
            case R.id.action_help:
                mEventBus.post(new AnalyticsScreenEvent(SCREEN_HELP, getDeviceLocale(this)));
                WebUrlLauncher.openUrl(this, URI_HELP);
                return true;
            case R.id.action_rate:
                openPlayStore();
                return true;
            case R.id.action_share:
                launchShare();
                return true;
            case R.id.action_share_story:
                launchShareStory();
                return true;
            case R.id.action_contact_us:
                launchContactUs();
                return true;
            case R.id.action_terms_of_use:
                mEventBus.post(new AnalyticsScreenEvent(SCREEN_TERMS_OF_USE, getDeviceLocale(this)));
                WebUrlLauncher.openUrl(this, URI_TERMS_OF_USE);
                return true;
            case R.id.action_privacy_policy:
                mEventBus.post(new AnalyticsScreenEvent(SCREEN_PRIVACY_POLICY, getDeviceLocale(this)));
                WebUrlLauncher.openUrl(this, URI_PRIVACY);
                return true;
            case R.id.action_copyright:
                mEventBus.post(new AnalyticsScreenEvent(SCREEN_COPYRIGHT, getDeviceLocale(this)));
                WebUrlLauncher.openUrl(this, URI_COPYRIGHT);
                return true;
        }

        return onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
        stopLanguagesChangeListener();
    }

    /* END lifecycle */

    @NonNull
    protected Settings prefs() {
        return Settings.getInstance(this);
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

            mLoginItem = mDrawerMenu.getMenu().findItem(R.id.action_login);
            mSignupItem = mDrawerMenu.getMenu().findItem(R.id.action_signup);
            mLogoutItem = mDrawerMenu.getMenu().findItem(R.id.action_logout);
            updateNavigationDrawerMenu();
        }
    }

    /**
     * This method is used to update the Navigation Draws Login settings.
     * It will only show log in information if the device is set to english
     * and display log in links based on users status.
     * Updated by:  Gyasi Story
     */
    private void updateNavigationDrawerMenu() {
        if (!containsLoginLanguage()) {  // For non English
            if (mLoginItem != null) {
                mLoginItem.setVisible(false);
            }
            if (mSignupItem != null) {
                mSignupItem.setVisible(false);
            }
            if (mLogoutItem != null) {
                mLogoutItem.setVisible(false);
            }

        } else { // For English
            Boolean isNotLoggedIn = mTheKey.getDefaultSessionGuid() == null;
            if (mLoginItem != null) {
                mLoginItem.setVisible(isNotLoggedIn);
            }
            if (mSignupItem != null) {
                mSignupItem.setVisible(isNotLoggedIn);
            }
            if (mLogoutItem != null) {
                mLogoutItem.setVisible(!isNotLoggedIn);
            }
        }
    }

    /**
     * This method will determine if the application contains
     * a supported language for Login Notifications. As we
     * get more support for more languages you can add them to {@code languages}.
     *
     * @return Boolean value if login Language
     *
     * Updated by: Gyasi Story
     */
    private boolean containsLoginLanguage() {
        LocaleListCompat localeListCompat = ConfigurationCompat
                .getLocales(Resources.getSystem().getConfiguration());
        String matchLanguage = localeListCompat.getFirstMatch(mLoginLanguages).getLanguage();
            for (String lang : mLoginLanguages) {
                if (matchLanguage.equals(lang)) {
                    return true;
                }
            }
        return false;

    }

    protected final void closeNavigationDrawer() {
        closeNavigationDrawer(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED));
    }

    protected final void closeNavigationDrawer(final boolean animate) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START, animate);
        }
    }

    protected boolean showNavigationDrawerIndicator() {
        return false;
    }

    void loadLanguages(final boolean initial) {
        final Settings settings = prefs();
        final Locale oldPrimary = mPrimaryLanguage;
        mPrimaryLanguage = settings.getPrimaryLanguage();
        final Locale oldParallel = mParallelLanguage;
        mParallelLanguage = settings.getParallelLanguage();

        // trigger lifecycle events
        if (!initial) {
            if (!Objects.equal(oldPrimary, mPrimaryLanguage)) {
                onUpdatePrimaryLanguage();
            }
            if (!Objects.equal(oldParallel, mParallelLanguage)) {
                onUpdateParallelLanguage();
            }
        }
    }

    private void startLanguagesChangeListener() {
        prefs().registerOnSharedPreferenceChangeListener(mSettingsChangeListener);
    }

    private void stopLanguagesChangeListener() {
        prefs().unregisterOnSharedPreferenceChangeListener(mSettingsChangeListener);
    }

    /* Navigation Menu actions */

    private void openPlayStore() {
        final String appId = BuildConfig.APPLICATION_ID;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
        }
    }

    private void launchLogin(final boolean signup) {
        final Uri redirectUri = new Uri.Builder()
                .scheme("https")
                .authority(getString(R.string.account_deeplink_host))
                .path(getString(R.string.account_deeplink_path))
                .build();

        // try using an external browser first if we will deeplink back to GodTools
        boolean handled = false;
        if (ComponentNameUtils.isDefaultComponentFor(this, MainActivity.class, redirectUri)) {
            handled = WebUrlLauncher.openUrl(this, mTheKey.loginUriBuilder()
                    .redirectUri(redirectUri)
                    .signup(signup)
                    .build());
        }

        // fallback to an in-app DialogFragment for login
        if (!handled) {
            final FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag(TAG_KEY_LOGIN_DIALOG) == null) {
                LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder()
                        .redirectUri(redirectUri)
                        .signup(signup)
                        .build();
                loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), TAG_KEY_LOGIN_DIALOG);
            }
        }
    }

    private void launchContactUs() {
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_CONTACT_US, getDeviceLocale(this)));
        final Intent intent = new Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT);
        try {
            startActivity(intent);
        } catch (@NonNull final ActivityNotFoundException e) {
            WebUrlLauncher.openUrl(this, URI_SUPPORT);
        }
    }

    private void launchShare() {
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_SHARE_GODTOOLS, mPrimaryLanguage));
        final String shareLink = URI_SHARE_BASE.buildUpon()
                .appendPath(LocaleCompat.toLanguageTag(mPrimaryLanguage).toLowerCase())
                .appendPath("")
                .build().toString();

        final String text = getString(R.string.share_general_message)
                .replace(SHARE_LINK, shareLink);

        final Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));
    }

    private void launchShareStory() {
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_SHARE_STORY, getDeviceLocale(this)));
        final Intent intent = new Intent(Intent.ACTION_SENDTO, MAILTO_SUPPORT);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_story_subject));
        try {
            startActivity(intent);
        } catch (@NonNull final ActivityNotFoundException e) {
            WebUrlLauncher.openUrl(this, URI_SUPPORT);
        }
    }

    protected void showLanguageSettings() {
        LanguageSettingsActivity.start(this);
    }

    class ChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(@Nullable final SharedPreferences preferences,
                                              @Nullable final String key) {
            switch (Strings.nullToEmpty(key)) {
                case PREF_PRIMARY_LANGUAGE:
                case PREF_PARALLEL_LANGUAGE:
                    loadLanguages(false);
            }
        }
    }
}
