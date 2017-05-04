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
    @NonNull
    public static SharedPreferences getSettings(@NonNull final Context context) {
        return context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
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

    public static void setPrimaryLanguage(@NonNull final Context context, @Nullable Locale locale) {
        setPrimaryLanguage(getSettings(context), locale);
    }

    public static void setPrimaryLanguage(@NonNull final SharedPreferences prefs, @Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        prefs.edit()
                .putString(PREF_PRIMARY_LANGUAGE, LocaleCompat.toLanguageTag(locale))
                .apply();
    }

    @Nullable
    public static Locale getParallelLanguage(@NonNull final Context context) {
        return getPrimaryLanguage(getSettings(context));
    }

    @Nullable
    public static Locale getParallelLanguage(@NonNull final SharedPreferences prefs) {
        final String raw = prefs.getString(PREF_PARALLEL_LANGUAGE, null);
        return raw != null ? LocaleCompat.forLanguageTag(raw) : null;
    }
}
