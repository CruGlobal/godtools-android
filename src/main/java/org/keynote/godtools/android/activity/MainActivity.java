package org.keynote.godtools.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.fragment.ToolsFragment;

public class MainActivity extends BaseActivity implements ToolsFragment.Callbacks {
    private static final String EXTRA_TOUR_LAUNCHED = MainActivity.class.getName() + ".TOUR_LAUNCHED";

    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    private static final int REQUEST_TOUR = 101;

    private boolean mTourLaunched = false;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);

        if (savedInstanceState != null) {
            mTourLaunched = savedInstanceState.getBoolean(EXTRA_TOUR_LAUNCHED, mTourLaunched);
        }
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
                AddResourcesActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResourceSelect(final long id) {
        // TODO
    }

    @Override
    public void onResourceInfo(final long id) {
        ToolDetailsActivity.start(this, id);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_TOUR_LAUNCHED, mTourLaunched);
    }

    /* END lifecycle */

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

        // update the displayed fragment
        fm.beginTransaction()
                .replace(R.id.frame, ToolsFragment.newAddedInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }

    private void showTourIfNeeded() {
        if (!prefs().isTourCompleted() && !mTourLaunched) {
            mTourLaunched = true;
            TourActivity.startForResult(this, REQUEST_TOUR);
        }
    }
}
