package org.cru.godtools.base.ui.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.res.ResourcesCompat
import java.util.Locale
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.ui.R

private val typefaces = buildMap {
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

private val FONT_SINHALA = FontFamily(Font(R.font.noto_sans_sinhala_regular))
private val FONT_TIBETAN = FontFamily(Font(R.font.noto_sans_tibetan_regular))

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

internal fun Locale.getFontFamilyOrNull() = sequenceOf(this).includeFallbacks()
    .mapNotNull {
        when (it) {
            Locale("si") -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) FONT_SINHALA else null
            Locale("bo") -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) FONT_TIBETAN else null
            else -> null
        }
    }
    .firstOrNull()
