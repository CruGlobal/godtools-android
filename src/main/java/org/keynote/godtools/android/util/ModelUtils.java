package org.keynote.godtools.android.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

public final class ModelUtils {
    @NonNull
    public static String getTranslationName(@Nullable final Translation translation,
                                            @Nullable final Resource resource) {
        return getTranslationName(translation != null ? translation.getName() : null,
                                  resource != null ? resource.getName() : null);
    }

    @NonNull
    public static String getTranslationName(@Nullable final String translationName,
                                            @Nullable final String resourceName) {
        return MoreObjects.firstNonNull(translationName, Strings.nullToEmpty(resourceName));
    }
}
