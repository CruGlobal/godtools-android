package org.cru.godtools.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LanguageTestUtils {
    // XXX: workaround for https://github.com/nhaarman/mockito-kotlin/issues/240
    public static String getDisplayName(@NonNull Language language, @Nullable Context context) {
        return language.getDisplayName(context);
    }
}
