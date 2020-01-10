package org.cru.godtools.base.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import timber.log.Timber;

import static org.ccci.gto.android.common.util.LocaleUtils.getOptionalDisplayName;

public class LocaleUtils {
    private static final String STRING_RES_LANGUAGE_NAME_PREFIX = "language_name_";

    public static Locale getDeviceLocale(@NonNull final Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }

    @NonNull
    public static String getDisplayName(@NonNull final Locale locale, @Nullable Context context,
                                        @Nullable final String defaultName, @Nullable final Locale inLocale) {
        // check for a language name string resource
        if (context != null) {
            if (inLocale != null) {
                context = localizeContextIfPossible(context, inLocale);
            }
            final String name = getLanguageNameStringRes(context, locale);
            if (name != null) {
                return name;
            }
        }

        // use Locale.getDisplayName()
        final String name = getOptionalDisplayName(locale, inLocale);
        if (name != null) {
            return name;
        }

        // use the default name if specified
        if (defaultName != null) {
            return defaultName;
        }

        // just rely on Locale.getDisplayName() which will default to the language code at this point
        Timber.tag("LocaleUtils")
                .e(new RuntimeException("Unable to find display name for " + locale.toString()),
                   "LocaleUtils.getDisplayName(%s, %s)", locale, inLocale);
        return inLocale != null ? locale.getDisplayName(inLocale) : locale.getDisplayName();
    }

    @NonNull
    public static Context localizeContextIfPossible(@NonNull final Context context, @NonNull final Locale locale) {
        final Configuration conf = new Configuration(context.getResources().getConfiguration());
        conf.setLocale(locale);
        return context.createConfigurationContext(conf);
    }

    @Nullable
    private static String getLanguageNameStringRes(@NonNull final Context context, @NonNull final Locale locale) {
        final Resources resources = context.getResources();
        final int stringId = resources
                .getIdentifier(STRING_RES_LANGUAGE_NAME_PREFIX + locale.toString().toLowerCase(Locale.ENGLISH),
                               "string", context.getPackageName());
        if (stringId == 0) {
            return null;
        }

        return resources.getString(stringId);
    }
}
