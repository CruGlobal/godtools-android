@file:JvmName("LocaleTypefaceUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import androidx.core.content.res.ResourcesCompat
import java.util.Locale
import org.cru.godtools.base.ui.R

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

fun CharSequence.applyTypefaceSpan(typeface: Typeface?) = when {
    typeface == null -> this
    length == 0 -> this
    else -> {
        (this as? Spannable ?: SpannableString(this)).apply {
            setSpan(TypefaceSpan(typeface), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
