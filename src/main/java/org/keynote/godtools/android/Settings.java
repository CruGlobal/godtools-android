package org.keynote.godtools.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.compat.util.LocaleCompat;

import java.util.Locale;

import static org.keynote.godtools.android.Constants.PREFS_SETTINGS;
import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

public final class Settings {
    private static final String PREF_TOUR_COMPLETED = "tour_completed";

    @NonNull
    private final SharedPreferences mPrefs;

    private Settings(@NonNull final Context context) {
        mPrefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    @Nullable
    private static Settings sInstance;
    public static Settings getInstance(@NonNull final Context context) {
        synchronized (Settings.class) {
            if (sInstance == null) {
                sInstance = new Settings(context);
            }
        }
        return sInstance;
    }

    public boolean isTourCompleted() {
        return mPrefs.getBoolean(PREF_TOUR_COMPLETED, false);
    }

    public void setTourCompleted() {
        mPrefs.edit()
                .putBoolean(PREF_TOUR_COMPLETED, true)
                .apply();
    }

    @NonNull
    public Locale getPrimaryLanguage() {
        final String raw = mPrefs.getString(PREF_PRIMARY_LANGUAGE, null);
        final Locale locale = raw != null ? LocaleCompat.forLanguageTag(raw) : getDefaultLanguage();
        if (raw == null) {
            setPrimaryLanguage(locale);
        }
        return locale;
    }

    public void setPrimaryLanguage(@Nullable Locale locale) {
        if (locale == null) {
            locale = getDefaultLanguage();
        }

        // update the primary language, and remove the parallel language if it is the new primary language
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_PRIMARY_LANGUAGE, LocaleCompat.toLanguageTag(locale));
        if (locale.equals(getParallelLanguage())) {
            editor.remove(PREF_PARALLEL_LANGUAGE);
        }
        editor.apply();
    }

    @Nullable
    public Locale getParallelLanguage() {
        final String raw = mPrefs.getString(PREF_PARALLEL_LANGUAGE, null);
        return raw != null ? LocaleCompat.forLanguageTag(raw) : null;
    }

    public void setParallelLanguage(@Nullable final Locale locale) {
        // short-circuit if the specified language is currently the primary language
        if (getPrimaryLanguage().equals(locale)) {
            return;
        }

        mPrefs.edit()
                .putString(PREF_PARALLEL_LANGUAGE, locale != null ? LocaleCompat.toLanguageTag(locale) : null)
                .apply();
    }

    @NonNull
    public static Locale getDefaultLanguage() {
        return Locale.ENGLISH;
    }

    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean isLanguageProtected(@Nullable final Locale locale) {
        return locale != null &&
                !locale.equals(getDefaultLanguage()) &&
                !locale.equals(getPrimaryLanguage()) &&
                !locale.equals(getParallelLanguage());
    }
}
