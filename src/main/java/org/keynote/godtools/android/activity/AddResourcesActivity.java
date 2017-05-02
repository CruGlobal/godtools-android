package org.keynote.godtools.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.fragment.ResourcesFragment;

public class AddResourcesActivity extends BaseActivity implements ResourcesFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Context context) {
        context.startActivity(new Intent(context, AddResourcesActivity.class));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_resources, menu);
        return true;
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_langauge:
                LanguageSettingsActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    public void onResourceSelect(long id) {
        // TODO
    }

    @Override
    public void onResourceInfo(long id) {
        // TODO
    }

    /* END lifecycle */

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
                .replace(R.id.frame, ResourcesFragment.newAvailableInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
