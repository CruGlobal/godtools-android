package org.cru.godtools.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

import org.cru.godtools.R;
import org.keynote.godtools.android.fragment.ToolsFragment;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import static org.cru.godtools.analytics.AnalyticsService.SCREEN_ADD_TOOLS;

public class AddToolsActivity extends BasePlatformActivity implements ToolsFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Context context) {
        context.startActivity(new Intent(context, AddToolsActivity.class));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_tools, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnalytics.onTrackScreen(SCREEN_ADD_TOOLS);
    }

    @Override
    public void onToolSelect(@Nullable final String code, @NonNull final Tool.Type type, final Locale... languages) {
        ToolDetailsActivity.start(this, code);
    }

    @Override
    public void onToolInfo(@Nullable final String code) {
        ToolDetailsActivity.start(this, code);
    }

    @Override
    public void onNoToolsAvailableAction() {
        finish();
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
                .replace(R.id.frame, ToolsFragment.newAvailableInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
