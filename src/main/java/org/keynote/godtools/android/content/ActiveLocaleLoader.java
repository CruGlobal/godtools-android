package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.support.v4.content.AsyncTaskSharedPreferencesChangeLoader;

import java.util.Locale;

import static org.keynote.godtools.android.Constants.PREFS_SETTINGS;
import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

public class ActiveLocaleLoader extends AsyncTaskSharedPreferencesChangeLoader<Locale> {
    private final boolean mPrimary;

    public ActiveLocaleLoader(@NonNull final Context context, final boolean primary) {
        super(context, PREFS_SETTINGS);
        mPrimary = primary;

        addPreferenceKey(prefName());
    }

    /* BEGIN lifecycle */

    @Override
    protected void onStartLoading() {
        deliverResult(getCurrentLocale());
        super.onStartLoading();
    }

    /* END lifecycle */

    private String prefName() {
        return mPrimary ? PREF_PRIMARY_LANGUAGE : PREF_PARALLEL_LANGUAGE;
    }

    @Nullable
    @Override
    public Locale loadInBackground() {
        return getCurrentLocale();
    }

    @Nullable
    private Locale getCurrentLocale() {
        final String raw = mPrefs.getString(prefName(), null);
        return raw != null ? LocaleCompat.forLanguageTag(raw) : null;
    }
}
