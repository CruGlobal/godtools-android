package org.cru.godtools.base.util

import android.content.Context
import java.text.Collator
import java.util.Locale
import org.ccci.gto.android.common.util.content.localizeIfPossible
import org.ccci.gto.android.common.util.getOptionalDisplayName
import org.cru.godtools.base.R
import timber.log.Timber

fun Locale.getPrimaryCollator(): Collator = Collator.getInstance(this).also { it.strength = Collator.PRIMARY }

fun Locale.getDisplayName(context: Context? = null, defaultName: String? = null, inLocale: Locale? = null): String {
    return context?.localizeIfPossible(inLocale)?.getLanguageNameStringRes(this)
        // use Locale.getDisplayName()
        ?: getOptionalDisplayName(inLocale)
        // use the default name if specified
        ?: defaultName
        // just rely on Locale.getDisplayName() which will default to the language code at this point
        ?: run {
            val e = RuntimeException("Unable to find display name for $this")
            Timber.tag("LocaleUtils")
                .e(e, "Locale(%s).getDisplayName(defaultName = %s, inLocale = %s)", this, defaultName, inLocale)
            if (inLocale != null) getDisplayName(inLocale) else displayName
        }
}

private fun Context.getLanguageNameStringRes(locale: Locale) = when (locale.toLanguageTag()) {
    "fa" -> getString(R.string.language_name_fa)
    "fil" -> getString(R.string.language_name_fil)
    else -> null
}
