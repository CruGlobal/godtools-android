package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.keynote.godtools.android.Settings;

import java.util.Locale;

import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

public abstract class BaseFragment extends Fragment {
    @Nullable
    private SharedPreferences mPrefs;
    private final ChangeListener mPrefsChangeListener = new ChangeListener();

    @NonNull
    protected Locale mPrimaryLanguage = Locale.getDefault();
    @Nullable
    protected Locale mParallelLanguage;

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context != null && mPrefs == null) {
            mPrefs = Settings.getSettings(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLanguages(true);
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

    /* END lifecycle */

    void loadLanguages(final boolean initial) {
        if (mPrefs != null) {
            final Locale oldPrimary = mPrimaryLanguage;
            mPrimaryLanguage = Settings.getPrimaryLanguage(mPrefs);
            final Locale oldParallel = mParallelLanguage;
            mParallelLanguage = Settings.getParallelLanguage(mPrefs);

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
        if (mPrefs != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangeListener);
        }
    }

    private void stopLanguagesChangeListener() {
        if (mPrefs != null) {
            mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefsChangeListener);
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
