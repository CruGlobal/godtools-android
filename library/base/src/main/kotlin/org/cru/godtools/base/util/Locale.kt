package org.cru.godtools.base.util

import android.content.Context
import androidx.annotation.StringRes
import java.text.Collator
import java.util.Locale
import org.ccci.gto.android.common.util.content.localizeIfPossible
import org.ccci.gto.android.common.util.getOptionalDisplayName
import org.cru.godtools.base.R
import timber.log.Timber

fun Locale.getPrimaryCollator(): Collator = Collator.getInstance(this).also { it.strength = Collator.PRIMARY }

fun Locale.getDisplayName(context: Context? = null, defaultName: String? = null, inLocale: Locale? = null): String {
    return getLanguageNameStringRes(context, inLocale)
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

private fun Locale.getLanguageNameStringRes(context: Context?, inLocale: Locale?) = when (toLanguageTag()) {
    "fa" -> context?.getLocalizedString(inLocale, R.string.language_name_fa)
    "fil" -> context?.getLocalizedString(inLocale, R.string.language_name_fil)
    else -> null
}

private fun Context.getLocalizedString(inLocale: Locale?, @StringRes resId: Int) =
    localizeIfPossible(inLocale).getString(resId)

fun Collection<Locale>.filterByDisplayAndNativeName(
    query: String,
    context: Context? = null,
    inLocale: Locale? = null,
): List<Locale> {
    val terms = query.split(Regex("\\s+")).filter { it.isNotBlank() }
    return filter {
        val displayName by lazy { it.getDisplayName(context, inLocale = inLocale) }
        val nativeName by lazy { it.getDisplayName(context, inLocale = it) }
        terms.all { displayName.contains(it, true) || nativeName.contains(it, true) }
    }
}
