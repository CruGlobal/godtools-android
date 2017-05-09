package org.keynote.godtools.android;

import android.content.Context;
import android.content.SharedPreferences;
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
    public static SharedPreferences getSettings(@NonNull final Context context) {
        return context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public static boolean isTourCompleted(@NonNull final Context context) {
        return getSettings(context).getBoolean(PREF_TOUR_COMPLETED, false);
    }

    public static void setTourCompleted(@NonNull final Context context) {
        getSettings(context).edit().putBoolean(PREF_TOUR_COMPLETED, true).apply();
    }

    @NonNull
    public static Locale getPrimaryLanguage(@NonNull final Context context) {
        return getPrimaryLanguage(getSettings(context));
    }

    @NonNull
    public static Locale getPrimaryLanguage(@NonNull final SharedPreferences prefs) {
        final String raw = prefs.getString(PREF_PRIMARY_LANGUAGE, null);
        final Locale locale = raw != null ? LocaleCompat.forLanguageTag(raw) : Locale.getDefault();
        if (raw == null) {
            setPrimaryLanguage(prefs, locale);
        }
        return locale;
    }

    public static void setPrimaryLanguage(@NonNull final Context context, @Nullable final Locale locale) {
        setPrimaryLanguage(getSettings(context), locale);
    }

    public static void setPrimaryLanguage(@NonNull final SharedPreferences prefs, @Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        // update the primary language, and remove the parallel language if it is the new primary language
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_PRIMARY_LANGUAGE, LocaleCompat.toLanguageTag(locale));
        if (locale.equals(getParallelLanguage(prefs))) {
            editor.remove(PREF_PARALLEL_LANGUAGE);
        }
        editor.apply();
    }

    @Nullable
    public static Locale getParallelLanguage(@NonNull final Context context) {
        return getParallelLanguage(getSettings(context));
    }

    @Nullable
    public static Locale getParallelLanguage(@NonNull final SharedPreferences prefs) {
        final String raw = prefs.getString(PREF_PARALLEL_LANGUAGE, null);
        return raw != null ? LocaleCompat.forLanguageTag(raw) : null;
    }

    public static void setParallelLanguage(@NonNull final Context context, @Nullable final Locale locale) {
        setParallelLanguage(getSettings(context), locale);
    }

    public static void setParallelLanguage(@NonNull final SharedPreferences prefs, @Nullable final Locale locale) {
        // short-circuit if the specified language is currently the primary language
        if (getPrimaryLanguage(prefs).equals(locale)) {
            return;
        }

        prefs.edit()
                .putString(PREF_PARALLEL_LANGUAGE, locale != null ? LocaleCompat.toLanguageTag(locale) : null)
                .apply();
    }
}
