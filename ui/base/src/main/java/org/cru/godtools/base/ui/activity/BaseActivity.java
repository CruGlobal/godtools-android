package org.cru.godtools.base.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.google.common.base.Objects;

import org.cru.godtools.base.ui.R2;
import org.greenrobot.eventbus.EventBus;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String EXTRA_LAUNCHING_COMPONENT = "org.cru.godtools.BaseActivity.launchingComponent";

    // App/Action Bar
    @Nullable
    @BindView(R2.id.appbar)
    protected Toolbar mToolbar;
    @Nullable
    protected ActionBar mActionBar;

    @NonNull
    protected /*final*/ EventBus mEventBus;

    @NonNull
    public static Bundle buildExtras(@NonNull final Context context) {
        final Bundle extras = new Bundle();
        if (context instanceof Activity) {
            extras.putParcelable(EXTRA_LAUNCHING_COMPONENT, ((Activity) context).getComponentName());
        }
        return extras;
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = EventBus.getDefault();
    }

    @Override
    @CallSuper
    public void onContentChanged() {
        super.onContentChanged();

        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        new BaseActivity_ViewBinding(this);

        ButterKnife.bind(this);
        setupActionBar();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        // update the Launching Component extra
        getIntent()
                .putExtra(EXTRA_LAUNCHING_COMPONENT, (Parcelable) intent.getParcelableExtra(EXTRA_LAUNCHING_COMPONENT));
    }

    @CallSuper
    protected void onSetupActionBar() {}

    // endregion Lifecycle Events

    private void setupActionBar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // trigger lifecycle event for subclasses
        onSetupActionBar();
    }

    // region Up Navigation

    @Override
    public void supportNavigateUpTo(@NonNull final Intent upIntent) {
        // if the upIntent already points to the original launching activity, just finish this activity
        if (Objects.equal(getIntent().getParcelableExtra(EXTRA_LAUNCHING_COMPONENT), upIntent.getComponent())) {
            finish();
            return;
        }

        // otherwise defer to default navigate behavior
        super.supportNavigateUpTo(upIntent);
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        final Intent intent = super.getSupportParentActivityIntent();

        if (intent != null) {
            intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(buildParentIntentExtras());
        }

        return intent;
    }

    @NonNull
    @CallSuper
    protected Bundle buildParentIntentExtras() {
        return new Bundle();
    }

    // endregion Up Navigation
}
