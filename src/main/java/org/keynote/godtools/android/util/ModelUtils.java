package org.keynote.godtools.android.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.model.Tool;

public final class ModelUtils {
    @NonNull
    public static String getTranslationName(@Nullable final Translation translation,
                                            @Nullable final Tool tool) {
        return getTranslationName(translation != null ? translation.getName() : null,
                                  tool != null ? tool.getName() : null);
    }

    @NonNull
    public static String getTranslationName(@Nullable final String translationName, @Nullable final String toolName) {
        return MoreObjects.firstNonNull(translationName, Strings.nullToEmpty(toolName));
    }
}
