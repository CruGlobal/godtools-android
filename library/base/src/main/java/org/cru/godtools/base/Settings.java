package org.cru.godtools.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.compat.util.LocaleCompat;

import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.thekey.android.TheKey;
import timber.log.Timber;

public final class Settings {
    private static final String PREFS_SETTINGS = "GodTools";
    public static final String PREF_PRIMARY_LANGUAGE = "languagePrimary";
    public static final String PREF_PARALLEL_LANGUAGE = "languageParallel";
    public static final String PREF_FEATURE_DISCOVERED = "feature_discovered.";
    private static final String PREF_ADDED_TO_CAMPAIGN = "added_to_campaign.";

    // feature discovery
    public static final String FEATURE_LANGUAGE_SETTINGS = "languageSettings";
    public static final String FEATURE_LOGIN = "login";
    public static final String FEATURE_TRACT_CARD_SWIPED = "tractCardSwiped";
    public static final String FEATURE_TRACT_CARD_CLICKED = "tractCardClicked";
    public static final String FEATURE_TUTORIAL_TRAINING = "tutorialTraining";
    public static final String FEATURE_TUTORIAL_ONBOARDING = "tutorialOnboarding";

    @NonNull
    private final Context mContext;
    @NonNull
    private final SharedPreferences mPrefs;

    private Settings(@NonNull final Context context) {
        mContext = context;
        mPrefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        trackFirstLaunchVersion();
    }

    @Nullable
    private static Settings sInstance;

    @NonNull
    public static Settings getInstance(@NonNull final Context context) {
        synchronized (Settings.class) {
            if (sInstance == null) {
                sInstance = new Settings(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    public boolean isFeatureDiscovered(@NonNull final String feature) {
        boolean discovered = isFeatureDiscoveredInt(feature);

        // handle pre-conditions that would indicate a feature was already discovered
        if (!discovered) {
            boolean changed = false;
            switch (feature) {
                case FEATURE_TUTORIAL_ONBOARDING:
                    if (getFirstLaunchVersion() <= VERSION_5_1_4 || Locale.getDefault() != Locale.ENGLISH) {
                        setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING);
                        changed = true;
                    }
                case FEATURE_LANGUAGE_SETTINGS:
                    if (getParallelLanguage() != null) {
                        setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS);
                        changed = true;
                    }
                    break;
                case FEATURE_LOGIN:
                    if (TheKey.getInstance(mContext).getDefaultSessionGuid() != null) {
                        setFeatureDiscovered(FEATURE_LOGIN);
                        changed = true;
                    }
                    break;
            }

            if (changed) {
                discovered = isFeatureDiscoveredInt(feature);
            }
        }

        return discovered;
    }

    private boolean isFeatureDiscoveredInt(@NonNull final String feature) {
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

    // region Version tracking
    private static final String PREF_VERSION_FIRST_LAUNCH = "version.firstLaunch";
    private static final int VERSION_5_1_4 = 4033503;

    private void trackFirstLaunchVersion() {
        if (mPrefs.contains(PREF_VERSION_FIRST_LAUNCH)) {
            return;
        }

        // The app was used before we started tracking the initial version, so just assume it was the most recent
        // version before we started tracking the first launch version
        if (mPrefs.contains(PREF_PRIMARY_LANGUAGE)) {
            mPrefs.edit().putInt(PREF_VERSION_FIRST_LAUNCH, VERSION_5_1_4).apply();
            return;
        }

        // resolve the current version code as the first launch code
        mPrefs.edit().putInt(PREF_VERSION_FIRST_LAUNCH, getCurrentVersion()).apply();
    }

    private int getCurrentVersion() {
        // lookup the current version from the package info
        try {
            final PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag("Settings").e(e, "Error retrieving the Package Info");
        }

        return -1;
    }

    private int getFirstLaunchVersion() {
        return mPrefs.getInt(PREF_VERSION_FIRST_LAUNCH, getCurrentVersion());
    }
    // endregion Version tracking

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
