package org.cru.godtools.base.util;

import android.content.Context;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

public class LocaleUtils {
    public static Locale getDeviceLocale(@NonNull final Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }

    @NonNull
    public static String getDisplayName(@NonNull final Locale locale) {
        return locale.getDisplayName();
    }
}
