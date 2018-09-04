package org.cru.godtools.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.compat.util.LocaleCompat;

import java.util.Locale;
import java.util.Set;

public final class Settings {
    private static final String PREFS_SETTINGS = "GodTools";
    public static final String PREF_PRIMARY_LANGUAGE = "languagePrimary";
    public static final String PREF_PARALLEL_LANGUAGE = "languageParallel";
    private static final String PREF_FEATURE_DISCOVERED = "feature_discovered.";
    private static final String PREF_ADDED_TO_CAMPAIGN = "added_to_campaign.";

    // feature discovery
    public static final String FEATURE_LANGUAGE_SETTINGS = "languageSettings";

    @NonNull
    private final SharedPreferences mPrefs;

    private Settings(@NonNull final Context context) {
        mPrefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    @Nullable
    private static Settings sInstance;

    @NonNull
    public static Settings getInstance(@NonNull final Context context) {
        synchronized (Settings.class) {
            if (sInstance == null) {
                sInstance = new Settings(context);
            }
        }
        return sInstance;
    }

    public boolean isFeatureDiscovered(@NonNull final String feature) {
        // handle pre-conditions that would indicate a feature was already discovered
        switch (feature) {
            case FEATURE_LANGUAGE_SETTINGS:
                if (getParallelLanguage() != null) {
                    setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS);
                }
                break;
        }

        return mPrefs.getBoolean(PREF_FEATURE_DISCOVERED + feature, false);
    }

    public void setFeatureDiscovered(@NonNull final String feature) {
        mPrefs.edit()
                .putBoolean(PREF_FEATURE_DISCOVERED + feature, true)
                .apply();
    }

    public boolean isAddedToCampaign(@NonNull final String guid) {
        return mPrefs.getBoolean(PREF_ADDED_TO_CAMPAIGN + guid.toUpperCase(Locale.ROOT), false);
    }

    public void setAddedToCampaign(@NonNull final String guid, final boolean added) {
        mPrefs.edit()
                .putBoolean(PREF_ADDED_TO_CAMPAIGN + guid.toUpperCase(Locale.ROOT), added)
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

    @NonNull
    public Set<Locale> getProtectedLanguages() {
        return ImmutableSet.of(getDefaultLanguage(), getPrimaryLanguage());
    }

    public boolean isLanguageProtected(@Nullable final Locale locale) {
        return getProtectedLanguages().contains(locale);
    }
}
