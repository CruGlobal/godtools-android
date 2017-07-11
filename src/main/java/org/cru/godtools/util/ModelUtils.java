package org.cru.godtools.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.cru.godtools.base.ui.util.LocaleTypefaceUtils;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.model.Tool;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

import static com.google.common.base.MoreObjects.firstNonNull;

public final class ModelUtils {
    @NonNull
    public static CharSequence getTranslationName(@Nullable final Context context,
                                                  @Nullable final Translation translation, @Nullable final Tool tool) {
        return getTranslationName(translation != null ? CalligraphyUtils.applyTypefaceSpan(
                translation.getName(), getTranslationTypeface(context, translation)) : null,
                                  tool != null ? tool.getName() : null);
    }

    @NonNull
    public static CharSequence getTranslationName(@Nullable final CharSequence translationName,
                                                  @Nullable final String toolName) {
        return firstNonNull(translationName, Strings.nullToEmpty(toolName));
    }

    @NonNull
    public static CharSequence getTranslationDescription(@Nullable final Context context,
                                                         @Nullable final Translation translation,
                                                         @Nullable final Tool tool) {
        return getTranslationDescription(translation != null ? CalligraphyUtils.applyTypefaceSpan(
                translation.getDescription(), getTranslationTypeface(context, translation)) : null,
                                         tool != null ? tool.getDescription() : null);
    }

    @NonNull
    public static CharSequence getTranslationDescription(@Nullable final CharSequence translationDescription,
                                                         @Nullable final String toolDescription) {
        return firstNonNull(translationDescription, Strings.nullToEmpty(toolDescription));
    }

    @Nullable
    private static Typeface getTranslationTypeface(@Nullable final Context context,
                                                   @NonNull final Translation translation) {
        return LocaleTypefaceUtils.getTypeface(context, translation.getLanguageCode());
    }
}
