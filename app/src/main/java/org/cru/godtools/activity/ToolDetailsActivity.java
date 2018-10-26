package org.cru.godtools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.cru.godtools.R;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.fragment.ToolDetailsFragment;
import org.cru.godtools.model.Tool;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_TOOL_DETAILS;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public class ToolDetailsActivity extends BasePlatformActivity implements ToolDetailsFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    @Nullable
    private /*final*/ String mTool = Tool.INVALID_CODE;

    public static void start(@NonNull final Activity context, @NonNull final String toolCode) {
        final Intent intent = new Intent(context, ToolDetailsActivity.class);
        intent.putExtra(EXTRA_TOOL, toolCode);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            mTool = extras.getString(EXTRA_TOOL, mTool);
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_generic_fragment_with_nav_drawer);
    }

    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_TOOL_DETAILS));
    }

    @Override
    public void onToolAdded() {
        finish();
    }

    @Override
    public void onToolRemoved() {
        finish();
    }

    /* END lifecycle */

    private boolean validStartState() {
        return mTool != null;
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
                .replace(R.id.frame, ToolDetailsFragment.newInstance(mTool), TAG_MAIN_FRAGMENT)
                .commit();
    }

    @Override
    public void supportNavigateUpTo(@NonNull final Intent upIntent) {
        finish();
    }
}
