package org.keynote.godtools.android.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;

import org.ccci.gto.android.common.util.LocaleCompat;

import java.util.Locale;
import java.util.Map;

public class TypefaceUtils {
    private static final Map<String, String> CUSTOM_TYPEFACES = ImmutableMap.of(
            "ta", "fonts/FreeSerif.ttf",
            "th", "fonts/FreeSerif.ttf",
            "ko", "fonts/UnGraphic.ttf",
            "bo", "fonts/Tibetan.ttf"
    );

    @NonNull
    public static <T extends TextView> T setTypeface(@NonNull final T view, @NonNull final Locale locale) {
        return setTypeface(view, LocaleCompat.toLanguageTag(locale));
    }

    @NonNull
    public static <T extends TextView> T setTypeface(@NonNull final T view, @NonNull final String language) {
        view.setTypeface(resolveTypeface(view.getContext(), language));
        return view;
    }

    @NonNull
    public static <T extends TextView> T setTypeface(@NonNull final T view, @NonNull final Locale locale,
                                                     final int style) {
        return setTypeface(view, LocaleCompat.toLanguageTag(locale), style);
    }

    @NonNull
    public static <T extends TextView> T setTypeface(@NonNull final T view, @NonNull final String language,
                                                     final int style) {
        view.setTypeface(resolveTypeface(view.getContext(), language), style);
        return view;
    }

    @Nullable
    private static Typeface resolveTypeface(@NonNull final Context context, @NonNull final String language) {
        if (CUSTOM_TYPEFACES.containsKey(language)) {
            return TypefaceCache.get(context, CUSTOM_TYPEFACES.get(language));
        } else {
            return null;
        }
    }
}
