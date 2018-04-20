package org.keynote.godtools.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.annimon.stream.Stream;

import org.cru.godtools.BuildConfig;
import org.cru.godtools.R;
import org.cru.godtools.activity.BasePlatformActivity;
import org.cru.godtools.activity.ToolDetailsActivity;
import org.cru.godtools.activity.TourActivity;
import org.cru.godtools.everystudent.EveryStudent;
import org.cru.godtools.init.content.task.InitialContentTasks;
import org.cru.godtools.sync.GodToolsSyncService;
import org.cru.godtools.tract.activity.TractActivity;
import org.cru.godtools.tract.service.TractManager;
import org.keynote.godtools.android.fragment.ToolsFragment;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import static android.arch.lifecycle.Lifecycle.State.STARTED;
import static org.cru.godtools.analytics.AnalyticsService.SCREEN_ADD_TOOLS;
import static org.cru.godtools.analytics.AnalyticsService.SCREEN_HOME;

public class MainActivity extends BasePlatformActivity implements ToolsFragment.Callbacks {
    private static final String EXTRA_TOUR_LAUNCHED = MainActivity.class.getName() + ".TOUR_LAUNCHED";
    private static final String EXTRA_ACTIVE_STATE = MainActivity.class.getName() + ".ACTIVE_STATE";

    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    private static final int REQUEST_TOUR = 101;

    private static final int STATE_MY_TOOLS = 0;
    private static final int STATE_FIND_TOOLS = 1;

    @Nullable
    private TabLayout.Tab mMyToolsTab;
    @Nullable
    private TabLayout.Tab mFindToolsTab;

    private int mActiveState = STATE_MY_TOOLS;
    private boolean mTourLaunched = false;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (savedInstanceState != null) {
            mActiveState = savedInstanceState.getInt(EXTRA_ACTIVE_STATE, mActiveState);
            mTourLaunched = savedInstanceState.getBoolean(EXTRA_TOUR_LAUNCHED, mTourLaunched);
        }

        // install any missing initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new InitialContentTasks(this));

        // sync any pending updates
        syncData();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
        showTourIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackInAnalytics();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        switch (requestCode) {
            case REQUEST_TOUR:
                prefs().setTourCompleted();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddTools();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        if (tab == mMyToolsTab) {
            showMyTools();
        } else if (tab == mFindToolsTab) {
            showAddTools();
        } else if (BuildConfig.DEBUG) {
            // The tab selection logic is brittle, so throw an error in unrecognized scenarios
            throw new IllegalArgumentException("Unrecognized tab!! something changed with the navigation tabs");
        }
    }

    @Override
    public void onToolSelect(@Nullable final String code, @NonNull final Tool.Type type, Locale... languages) {
        if (code != null) {
            switch (type) {
                case TRACT:
                    if (languages != null) {
                        languages = Stream.of(languages).withoutNulls().toArray(Locale[]::new);
                        if (languages.length > 0) {
                            // start preloading the tract in the first language
                            TractManager.getInstance(this).getLatestPublishedManifest(code, languages[0]);

                            TractActivity.start(this, code, languages);
                        }
                    }
                    break;
                case ARTICLE:
                    // hardcode everystudent content for now
                    if ("es".equals(code)) {
                        EveryStudent.start(this);
                    }
            }
        }
    }

    @Override
    public void onToolInfo(@Nullable final String code) {
        ToolDetailsActivity.start(this, code);
    }

    @Override
    public void onNoToolsAvailableAction() {
        showAddTools();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ACTIVE_STATE, mActiveState);
        outState.putBoolean(EXTRA_TOUR_LAUNCHED, mTourLaunched);
    }

    /* END lifecycle */

    @Override
    protected void setupNavigationTabs() {
        super.setupNavigationTabs();
        if (mNavigationTabs != null) {
            // This logic is brittle, so throw an error on debug builds if something changes.
            if (BuildConfig.DEBUG && mNavigationTabs.getTabCount() != 2) {
                throw new IllegalStateException("The navigation tabs changed!!! Logic needs to be updated!!!");
            }

            mMyToolsTab = mNavigationTabs.getTabAt(0);
            mFindToolsTab = mNavigationTabs.getTabAt(1);
        }
    }

    private void trackInAnalytics() {
        // only track analytics if this activity has been started
        if (getLifecycle().getCurrentState().isAtLeast(STARTED)) {
            switch (mActiveState) {
                case STATE_FIND_TOOLS:
                    mAnalytics.onTrackScreen(SCREEN_ADD_TOOLS);
                    break;
                case STATE_MY_TOOLS:
                default:
                    mAnalytics.onTrackScreen(SCREEN_HOME);
            }
        }
    }

    private void showAddTools() {
        // update the displayed fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, ToolsFragment.newAvailableInstance(), TAG_MAIN_FRAGMENT)
                .commit();

        selectNavigationTabIfNecessary(mFindToolsTab);
        mActiveState = STATE_FIND_TOOLS;
        trackInAnalytics();
    }

    private void showMyTools() {
        // update the displayed fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, ToolsFragment.newAddedInstance(), TAG_MAIN_FRAGMENT)
                .commit();

        selectNavigationTabIfNecessary(mMyToolsTab);
        mActiveState = STATE_MY_TOOLS;
        trackInAnalytics();
    }

    private void syncData() {
        GodToolsSyncService.syncFollowups(this).sync();
        GodToolsSyncService.syncToolShares(this).sync();
    }

    @Override
    protected boolean showNavigationDrawerIndicator() {
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
        showMyTools();
    }

    private void showTourIfNeeded() {
        if (!prefs().isTourCompleted() && !mTourLaunched) {
            mTourLaunched = true;
            TourActivity.startForResult(this, REQUEST_TOUR);
        }
    }
}
