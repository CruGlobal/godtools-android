@file:JvmName("LocaleUtils")

package org.cru.godtools.base.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.os.ConfigurationCompat
import java.util.Locale
import org.ccci.gto.android.common.util.content.localizeIfPossible
import org.ccci.gto.android.common.util.getOptionalDisplayName
import timber.log.Timber

@VisibleForTesting
internal const val STRING_RES_LANGUAGE_NAME_PREFIX = "language_name_"

val Context.deviceLocale get() = ConfigurationCompat.getLocales(resources.configuration)[0]

@JvmOverloads
fun Locale.getDisplayName(context: Context? = null, defaultName: String? = null, inLocale: Locale? = null): String {
    return context?.localizeIfPossible(inLocale)?.getLanguageNameStringRes(this)
        // use Locale.getDisplayName()
        ?: getOptionalDisplayName(inLocale)
        // use the default name if specified
        ?: defaultName
        // just rely on Locale.getDisplayName() which will default to the language code at this point
        ?: run {
            val e = RuntimeException("Unable to find display name for $this")
            Timber.tag("LocaleUtils").e(e, "LocaleUtils.getDisplayName(%s, %s)", this, inLocale)
            if (inLocale != null) getDisplayName(inLocale) else displayName
        }
}

private fun Context.getLanguageNameStringRes(locale: Locale) =
    when (val stringId = resources.getIdentifier(locale.languageNameStringRes, "string", packageName)) {
        0 -> null
        else -> resources.getString(stringId)
    }

private val Locale.languageNameStringRes
    get() = "$STRING_RES_LANGUAGE_NAME_PREFIX${toString().lowercase(Locale.ENGLISH)}"
