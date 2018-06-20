package org.cru.godtools.base.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.os.ConfigurationCompat;

import java.util.Locale;

public class LocaleUtils {
    public static Locale getDeviceLocale(@NonNull final Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }
}
