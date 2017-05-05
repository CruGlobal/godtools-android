package org.keynote.godtools.android.activity;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.Settings;
import org.keynote.godtools.android.fragment.ResourcesFragment;

public class MainActivity extends BaseActivity implements ResourcesFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);

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
        ResourceDetailsActivity.start(this, id);
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
                .replace(R.id.frame, ResourcesFragment.newAddedInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }

    private void showTourIfNeeded() {
        if (!Settings.isTourCompleted(this)) {
        }
    }
}
