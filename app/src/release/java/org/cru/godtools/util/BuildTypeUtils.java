package org.cru.godtools.util;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.cru.godtools.everystudent.EveryStudent;
import org.cru.godtools.model.Tool;

import java.util.Locale;

import static org.cru.godtools.model.Tool.CODE_EVERYSTUDENT;

public class BuildTypeUtils {
    public static void startArticleToolActivity(@NonNull final Activity activity, @NonNull final String code,
                                                @NonNull final Tool.Type type, @NonNull final Locale... languages) {
        // hardcode everystudent content for now
        if (CODE_EVERYSTUDENT.equals(code)) {
            EveryStudent.start(activity);
        }
    }
}
