package org.cru.godtools.base.ui.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.util.Locale

object LocaleTypefaceUtils {
    private val typefaces = mutableMapOf<Locale, String>().apply {
        // Sinhala, only needed for Android pre-Marshmallow
        //
        // https://forum.xda-developers.com/android/general/font-sinhala-font-android-5-0-lollipop-t3150536
        // http://www.xperiablog.net/2015/12/08/sony-confirms-sinhalese-language-support-in-android-6-0-update/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            put(Locale("si"), "fonts/NotoSansSinhala-Regular.ttf")
        }

        // Tibetan, added in Marshmallow
        //
        // http://digitaltibetan.org/index.php/Tibetan_support_in_Android_6_Marshmallow
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            put(Locale("bo"), "fonts/NotoSansTibetan-Regular.ttf")
        }
    }

    @JvmStatic
    fun getTypeface(context: Context?, locale: Locale?) = when {
        context != null && typefaces.containsKey(locale) -> TypefaceUtils.load(context.assets, typefaces[locale])
        else -> null
    }

    /**
     * This method works around a crash caused by setting a null Typeface span within Calligraphy.
     */
    @JvmStatic
    fun safeApplyTypefaceSpan(s: CharSequence?, typeface: Typeface?) = when {
        typeface != null -> CalligraphyUtils.applyTypefaceSpan(s, typeface)
        else -> null
    }
}
