package org.keynote.godtools.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.fragment.ToolDetailsFragment;
import org.keynote.godtools.android.model.Tool;

import static org.keynote.godtools.android.Constants.EXTRA_TOOL;

public class ToolDetailsActivity extends BaseActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    private long mTool = Tool.INVALID_ID;

    public static void start(@NonNull final Context context, final long resourceId) {
        final Intent intent = new Intent(context, ToolDetailsActivity.class);
        intent.putExtra(EXTRA_TOOL, resourceId);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);

        final Intent intent = getIntent();
        if (intent != null) {
            mTool = intent.getLongExtra(EXTRA_TOOL, mTool);
        }
    }

    @Override
    protected void onSetupActionBar(@NonNull final ActionBar actionBar) {
        super.onSetupActionBar(actionBar);
        setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
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
                .replace(R.id.frame, ToolDetailsFragment.newInstance(mTool), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
