package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.keynote.godtools.android.Settings;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

public abstract class BaseFragment extends Fragment {
    @Nullable
    Settings mSettings;
    private final ChangeListener mSettingsChangeListener = new ChangeListener();

    @Nullable
    private Unbinder mButterKnife;

    @NonNull
    protected Locale mPrimaryLanguage = Locale.getDefault();
    @Nullable
    protected Locale mParallelLanguage;

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context != null) {
            mSettings = Settings.getInstance(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLanguages(true);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButterKnife = ButterKnife.bind(this, view);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLanguagesChangeListener();
        loadLanguages(false);
    }

    protected void onUpdatePrimaryLanguage() {}

    protected void onUpdateParallelLanguage() {}

    @Override
    public void onStop() {
        super.onStop();
        stopLanguagesChangeListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mButterKnife != null) {
            mButterKnife.unbind();
        }
        mButterKnife = null;
    }

    /* END lifecycle */

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
