package org.cru.godtools.base.ui.util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public final class LocaleTypefaceUtils {
    private static final Map<Locale, String> TYPEFACES;
    static {
        final ImmutableMap.Builder<Locale, String> typefaceBuilder = ImmutableMap.builder();

        // Sinhala, only needed for Android pre-Marshmallow
        //
        // https://forum.xda-developers.com/android/general/font-sinhala-font-android-5-0-lollipop-t3150536
        // http://www.xperiablog.net/2015/12/08/sony-confirms-sinhalese-language-support-in-android-6-0-update/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            typefaceBuilder.put(new Locale("si"), "fonts/NotoSansSinhala-Regular.ttf");
        }

        // Tibetan, added in Marshmallow
        //
        // http://digitaltibetan.org/index.php/Tibetan_support_in_Android_6_Marshmallow
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            typefaceBuilder.put(new Locale("bo"), "fonts/NotoSansTibetan-Regular.ttf");
        }

        TYPEFACES = typefaceBuilder.build();
    }

    @Nullable
    public static Typeface getTypeface(@Nullable final Context context, @Nullable final Locale locale) {
        if (context != null && TYPEFACES.containsKey(locale)) {
            return TypefaceUtils.load(context.getAssets(), TYPEFACES.get(locale));
        }
        return null;
    }

    /**
     * This method works around a crash caused by setting a null Typeface span within Calligraphy.
     */
    @Nullable
    public static CharSequence safeApplyTypefaceSpan(@Nullable final CharSequence s,
                                                     @Nullable final Typeface typeface) {
        if (typeface != null) {
            return CalligraphyUtils.applyTypefaceSpan(s, typeface);
        } else {
            return s;
        }
    }
}
