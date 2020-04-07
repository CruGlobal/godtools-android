@file:JvmName("LocaleTypefaceUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import org.cru.godtools.base.ui.R
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import java.util.Locale

@OptIn(ExperimentalStdlibApi::class)
private val typefaces = buildMap<Locale, Int> {
    // Sinhala, only needed for Android pre-Marshmallow
    //
    // https://forum.xda-developers.com/android/general/font-sinhala-font-android-5-0-lollipop-t3150536
    // http://www.xperiablog.net/2015/12/08/sony-confirms-sinhalese-language-support-in-android-6-0-update/
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        put(Locale("si"), R.font.noto_sans_sinhala_regular)
    }

    // Tibetan, added in Marshmallow
    //
    // http://digitaltibetan.org/index.php/Tibetan_support_in_Android_6_Marshmallow
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        put(Locale("bo"), R.font.noto_sans_tibetan_regular)
    }
}

fun Context.getTypeface(locale: Locale?) = typefaces[locale]?.let { ResourcesCompat.getFont(this, it) }

// TODO: make CharSequence receiver non-null
@JvmName("safeApplyTypefaceSpan")
fun CharSequence?.applyTypefaceSpan(typeface: Typeface?) = when {
    // workaround a crash caused by setting a null Typeface span within Calligraphy.
    typeface != null -> CalligraphyUtils.applyTypefaceSpan(this, typeface)
    else -> this
}
