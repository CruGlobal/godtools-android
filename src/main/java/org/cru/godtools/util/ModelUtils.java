package org.cru.godtools.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.model.Tool;

public final class ModelUtils {
    @NonNull
    public static String getTranslationName(@Nullable final Translation translation, @Nullable final Tool tool) {
        return getTranslationName(translation != null ? translation.getName() : null,
                                  tool != null ? tool.getName() : null);
    }

    @NonNull
    public static String getTranslationName(@Nullable final String translationName, @Nullable final String toolName) {
        return MoreObjects.firstNonNull(translationName, Strings.nullToEmpty(toolName));
    }

    @NonNull
    public static String getTranslationDescription(@Nullable final Translation translation, @Nullable final Tool tool) {
        return getTranslationDescription(translation != null ? translation.getDescription() : null,
                                         tool != null ? tool.getDescription() : null);
    }

    @NonNull
    public static String getTranslationDescription(@Nullable final String translationDescription,
                                                   @Nullable final String toolDescription) {
        return MoreObjects.firstNonNull(translationDescription, Strings.nullToEmpty(toolDescription));
    }
}
