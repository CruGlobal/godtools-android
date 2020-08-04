package org.keynote.godtools.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.annimon.stream.Stream;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.tabs.TabLayout;

import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper;
import org.cru.godtools.BuildConfig;
import org.cru.godtools.R;
import org.cru.godtools.activity.BasePlatformActivity;
import org.cru.godtools.analytics.LaunchTrackingViewModel;
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.tool.service.ManifestManager;
import org.cru.godtools.base.util.LocaleUtils;
import org.cru.godtools.model.Tool;
import org.cru.godtools.tutorial.PageSet;
import org.cru.godtools.tutorial.activity.TutorialActivityKt;
import org.cru.godtools.ui.languages.LanguageSettingsActivityKt;
import org.cru.godtools.ui.tooldetails.ToolDetailsActivityKt;
import org.cru.godtools.ui.tools.ToolsFragment;
import org.cru.godtools.ui.tools.analytics.model.AboutToolButton;
import org.cru.godtools.util.ActivityUtilsKt;

import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import dagger.Lazy;
import me.thekey.android.core.CodeGrantAsyncTask;

import static androidx.lifecycle.Lifecycle.State.STARTED;
import static org.cru.godtools.analytics.firebase.model.FirebaseIamActionEventKt.ACTION_IAM_MY_TOOLS;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_ALL_TOOLS;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_HOME;
import static org.cru.godtools.base.Settings.FEATURE_LANGUAGE_SETTINGS;
import static org.cru.godtools.base.Settings.FEATURE_TUTORIAL_ONBOARDING;

public class MainActivity extends BasePlatformActivity implements ToolsFragment.Callbacks {
    private static final String EXTRA_ACTIVE_STATE = MainActivity.class.getName() + ".ACTIVE_STATE";

    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    private static final int STATE_MY_TOOLS = 0;
    private static final int STATE_FIND_TOOLS = 1;

    @Inject
    Lazy<ManifestManager> mManifestManager;

    @Nullable
    private TabLayout.Tab mFavoriteToolsTab;
    @Nullable
    private TabLayout.Tab mAllToolsTab;
    @Nullable
    TapTargetView mFeatureDiscovery;

    private int mActiveState = STATE_MY_TOOLS;

    // region Lifecycle
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        triggerOnboardingIfNecessary();
        setContentView(R.layout.activity_dashboard);

        processIntent(getIntent());

        if (savedInstanceState != null) {
            mActiveState = savedInstanceState.getInt(EXTRA_ACTIVE_STATE, mActiveState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackInAnalytics();
    }

    @Override
    protected void onSyncData(@NonNull final SwipeRefreshSyncHelper syncHelper, final boolean force) {
        super.onSyncData(syncHelper, force);
        getSyncService().syncFollowups().sync();
        getSyncService().syncToolShares().sync();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showAllTools();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        if (tab == mFavoriteToolsTab) {
            showFavoriteTools();
        } else if (tab == mAllToolsTab) {
            showAllTools();
        } else if (BuildConfig.DEBUG) {
            // The tab selection logic is brittle, so throw an error in unrecognized scenarios
            throw new IllegalArgumentException("Unrecognized tab!! something changed with the navigation tabs");
        }
    }

    @Override
    public void onToolSelect(@Nullable final String code, @NonNull final Tool.Type type,
                             @Nullable Locale... languages) {
        // short-circuit if we don't have a valid tool code
        if (code == null) {
            return;
        }

        // sanitize the languages list, and short-circuit if we don't have any languages
        if (languages != null) {
            languages = Stream.of(languages).withoutNulls().toArray(Locale[]::new);
        }
        if (languages == null || languages.length == 0) {
            return;
        }

        // start pre-loading the tool in the first language
        mManifestManager.get().preloadLatestPublishedManifest(code, languages[0]);

        ActivityUtilsKt.openToolActivity(this, code, type, languages);
    }

    @Override
    public void onToolInfo(@Nullable final String code) {
        if (code != null) {
            ToolDetailsActivityKt.startToolDetailsActivity(this, code);
            eventBus.post(AboutToolButton.INSTANCE);
        }
    }

    @Override
    public void onNoToolsAvailableAction() {
        showAllTools();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ACTIVE_STATE, mActiveState);
    }
    // endregion Lifecycle

    // region Onboarding
    private void triggerOnboardingIfNecessary() {
        // TODO: remove this once we support onboarding in all languages
        // mark OnBoarding as discovered if this isn't a supported language
        final Settings settings = getSettings();
        if (!PageSet.ONBOARDING.supportsLocale(LocaleUtils.getDeviceLocale(this))) {
            settings.setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING);
        }

        if (!settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) {
            TutorialActivityKt.startTutorialActivity(this, PageSet.ONBOARDING);
        }
    }
    // endregion Onboarding

    private void processIntent(@Nullable final Intent intent) {
        final String action = intent != null ? intent.getAction() : null;
        final Uri data = intent != null ? intent.getData() : null;
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            if (getString(R.string.account_deeplink_host).equalsIgnoreCase(data.getHost())) {
                if (getString(R.string.account_deeplink_path).equalsIgnoreCase(data.getPath())) {
                    new CodeGrantAsyncTask(getTheKey(), data).execute();
                    intent.setData(null);
                }
            }
        }
    }

    @Override
    protected void setupNavigationTabs() {
        super.setupNavigationTabs();
        if (navigationTabs != null) {
            // This logic is brittle, so throw an error on debug builds if something changes.
            if (BuildConfig.DEBUG && navigationTabs.getTabCount() != 2) {
                throw new IllegalStateException("The navigation tabs changed!!! Logic needs to be updated!!!");
            }

            mFavoriteToolsTab = navigationTabs.getTabAt(0);
            mAllToolsTab = navigationTabs.getTabAt(1);
        }
    }

    // region Analytics
    private void trackInAnalytics() {
        // only track analytics if this activity has been started
        if (getLifecycle().getCurrentState().isAtLeast(STARTED)) {
            switch (mActiveState) {
                case STATE_FIND_TOOLS:
                    eventBus.post(new AnalyticsScreenEvent(SCREEN_ALL_TOOLS));
                    break;
                case STATE_MY_TOOLS:
                default:
                    eventBus.post(new AnalyticsScreenEvent(SCREEN_HOME));
                    eventBus.post(new FirebaseIamActionEvent(ACTION_IAM_MY_TOOLS));
            }

            trackLaunch();
        }
    }

    private void trackLaunch() {
        (new ViewModelProvider(this)).get(LaunchTrackingViewModel.class).trackLaunch();
    }
    // endregion Analytics

    @Override
    protected boolean isShowNavigationDrawerIndicator() {
        return true;
    }

    @MainThread
    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        // short-circuit if there is a currently attached fragment
        Fragment fragment = fm.findFragmentByTag(TAG_MAIN_FRAGMENT);
        if (fragment != null) {
            return;
        }

        // default to My Tools
        showFavoriteTools();
    }

    private void showAllTools() {
        // update the displayed fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new ToolsFragment(ToolsFragment.MODE_ALL), TAG_MAIN_FRAGMENT)
                .commit();

        selectNavigationTabIfNecessary(mAllToolsTab);
        mActiveState = STATE_FIND_TOOLS;
        trackInAnalytics();
    }

    private void showFavoriteTools() {
        // update the displayed fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new ToolsFragment(ToolsFragment.MODE_ADDED), TAG_MAIN_FRAGMENT)
                .commit();

        selectNavigationTabIfNecessary(mFavoriteToolsTab);
        mActiveState = STATE_MY_TOOLS;
        trackInAnalytics();
    }

    // region Feature Discovery logic
    protected void showNextFeatureDiscovery() {
        if (!getSettings().isFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS) &&
                canShowFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS)) {
            dispatchDelayedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS, false, 15000);
            return;
        }

        super.showNextFeatureDiscovery();
    }

    protected boolean canShowFeatureDiscovery(@NonNull final String feature) {
        switch (feature) {
            case FEATURE_LANGUAGE_SETTINGS:
                return toolbar != null && (drawerLayout == null || !drawerLayout.isDrawerOpen(GravityCompat.START));
            default:
                return super.canShowFeatureDiscovery(feature);
        }
    }

    protected void onShowFeatureDiscovery(@NonNull final String feature, final boolean force) {
        // dispatch specific feature discovery
        switch (feature) {
            case FEATURE_LANGUAGE_SETTINGS:
                assert toolbar != null : "canShowFeatureDiscovery() verifies mToolbar is not null";
                if (toolbar.findViewById(R.id.action_switch_language) != null) {
                    // purge any pending feature discovery triggers since we are showing feature discovery now
                    purgeQueuedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS);

                    // show language settings feature discovery
                    final TapTarget target = TapTarget.forToolbarMenuItem(
                            toolbar, R.id.action_switch_language,
                            getString(R.string.feature_discovery_title_language_settings),
                            getString(R.string.feature_discovery_desc_language_settings));
                    mFeatureDiscovery =
                            TapTargetView.showFor(this, target, new LanguageSettingsFeatureDiscoveryListener());
                    setFeatureDiscoveryActive(feature);
                } else {
                    // TODO: we currently don't (can't?) distinguish between when the menu item doesn't exist and when
                    // TODO: the menu item just hasn't been drawn yet.

                    // the toolbar action isn't available yet.
                    // re-attempt this feature discovery on the next frame iteration.
                    dispatchDelayedFeatureDiscovery(feature, force, 17);
                }
                break;
            default:
                super.onShowFeatureDiscovery(feature, force);
                break;
        }
    }

    @Override
    protected boolean isFeatureDiscoveryVisible() {
        return super.isFeatureDiscoveryVisible() || mFeatureDiscovery != null;
    }
    // endregion Feature Discovery logic

    class LanguageSettingsFeatureDiscoveryListener extends TapTargetView.Listener {
        @Override
        public void onTargetClick(final TapTargetView view) {
            super.onTargetClick(view);
            LanguageSettingsActivityKt.startLanguageSettingsActivity(MainActivity.this);
        }

        @Override
        public void onOuterCircleClick(final TapTargetView view) {
            onTargetCancel(view);
        }

        @Override
        public void onTargetDismissed(final TapTargetView view, final boolean userInitiated) {
            super.onTargetDismissed(view, userInitiated);
            if (userInitiated) {
                getSettings().setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS);
                setFeatureDiscoveryActive(null);
                showNextFeatureDiscovery();
            }

            if (view == mFeatureDiscovery) {
                mFeatureDiscovery = null;
            }
        }
    }
}
