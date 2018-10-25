package org.cru.godtools.util;

import android.app.Activity;

import org.cru.godtools.article.activity.CategoriesActivity;
import org.cru.godtools.model.Tool;

import java.util.Locale;

import androidx.annotation.NonNull;

public class BuildTypeUtils {
    public static void startArticleToolActivity(@NonNull final Activity activity, @NonNull final String code,
                                                @NonNull final Tool.Type type, @NonNull final Locale... languages) {
        CategoriesActivity.start(activity, code, languages[0]);
    }
}
