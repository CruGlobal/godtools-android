package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.ccci.gto.android.sync.event.SyncFinishedEvent;
import org.ccci.gto.android.sync.widget.SwipeRefreshSyncHelper;
import org.cru.godtools.base.Settings;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

public abstract class BaseFragment extends Fragment {
    private static final String EXTRA_SYNC_HELPER = BaseFragment.class.getName() + ".SYNC_HELPER";

    @Nullable
    Settings mSettings;
    private final ChangeListener mSettingsChangeListener = new ChangeListener();

    @Nullable
    private Unbinder mButterKnife;

    @Nullable
    @BindView(R.id.refresh)
    SwipeRefreshLayout mRefreshLayout;
    final SwipeRefreshSyncHelper mSyncHelper = new SwipeRefreshSyncHelper();

    @NonNull
    protected Locale mPrimaryLanguage = Settings.getDefaultLanguage();
    @Nullable
    protected Locale mParallelLanguage;

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Context context) {
        Crashlytics.log(toString() + " onAttach()");
        super.onAttach(context);
        if (context != null) {
            mSettings = Settings.getInstance(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore any saved state
        if (savedInstanceState != null) {
            mSyncHelper.onRestoreInstanceState(savedInstanceState.getBundle(EXTRA_SYNC_HELPER));
        }

        loadLanguages(true);
        syncData(false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButterKnife = ButterKnife.bind(this, view);
        setupRefreshView();
    }

    @Override
    public void onStart() {
        Crashlytics.log(toString() + " onStart()");
        super.onStart();
        EventBus.getDefault().register(this);
        startLanguagesChangeListener();
        loadLanguages(false);
        mSyncHelper.updateState();
    }

    protected void onUpdatePrimaryLanguage() {}

    protected void onUpdateParallelLanguage() {}

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onSyncCompleted(@NonNull final SyncFinishedEvent event) {
        mSyncHelper.updateState();
    }

    @Override
    public void onStop() {
        Crashlytics.log(toString() + " onStop()");
        super.onStop();
        stopLanguagesChangeListener();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(EXTRA_SYNC_HELPER, mSyncHelper.onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupRefreshView();
        if (mButterKnife != null) {
            mButterKnife.unbind();
        }
        mButterKnife = null;
    }

    @Override
    public void onDetach() {
        Crashlytics.log(toString() + " onDetach()");
        super.onDetach();
    }

    /* END lifecycle */

    @CallSuper
    protected void syncData(final boolean force) {}

    void loadLanguages(final boolean initial) {
        if (mSettings != null) {
            final Locale oldPrimary = mPrimaryLanguage;
            mPrimaryLanguage = mSettings.getPrimaryLanguage();
            final Locale oldParallel = mParallelLanguage;
            mParallelLanguage = mSettings.getParallelLanguage();

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
    }

    private void startLanguagesChangeListener() {
        if (mSettings != null) {
            mSettings.registerOnSharedPreferenceChangeListener(mSettingsChangeListener);
        }
    }

    private void stopLanguagesChangeListener() {
        if (mSettings != null) {
            mSettings.unregisterOnSharedPreferenceChangeListener(mSettingsChangeListener);
        }
    }

    private void setupRefreshView() {
        mSyncHelper.setRefreshLayout(mRefreshLayout);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(() -> syncData(true));
        }
    }

    private void cleanupRefreshView() {
        mSyncHelper.setRefreshLayout(null);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(null);
        }
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
