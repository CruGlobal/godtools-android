@file:JvmName("LocaleUtils")

package org.cru.godtools.base.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import org.ccci.gto.android.common.util.LocaleUtils
import timber.log.Timber
import java.util.Locale

private const val STRING_RES_LANGUAGE_NAME_PREFIX = "language_name_"

val Context.deviceLocale: Locale get() = ConfigurationCompat.getLocales(resources.configuration)[0]

@JvmOverloads
fun Locale.getDisplayName(context: Context? = null, defaultName: String? = null, inLocale: Locale? = null): String {
    return context?.localizeIfPossible(inLocale)?.getLanguageNameStringRes(this)
        // use Locale.getDisplayName()
        ?: LocaleUtils.getOptionalDisplayName(this, inLocale)
        // use the default name if specified
        ?: defaultName
        // just rely on Locale.getDisplayName() which will default to the language code at this point
        ?: run {
            Timber.tag("LocaleUtils").e(
                RuntimeException("Unable to find display name for $this"),
                "LocaleUtils.getDisplayName(%s, %s)", this, inLocale
            )
            if (inLocale != null) getDisplayName(inLocale) else displayName
        }
}

@JvmName("localizeContextIfPossible")
fun Context.localizeIfPossible(locale: Locale?): Context = when (locale) {
    null -> this
    else -> createConfigurationContext(Configuration(resources.configuration).apply { setLocale(locale) })
}

private fun Context.getLanguageNameStringRes(locale: Locale): String? {
    return when (val stringId = resources.getIdentifier(
        "$STRING_RES_LANGUAGE_NAME_PREFIX${locale.toString().toLowerCase(Locale.ENGLISH)}", "string", packageName
    )) {
        0 -> null
        else -> resources.getString(stringId)
    }
}
