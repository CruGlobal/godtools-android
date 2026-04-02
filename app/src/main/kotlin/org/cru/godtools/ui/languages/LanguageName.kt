package org.cru.godtools.ui.languages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.model.Language

@Composable
internal fun LanguageName(locale: Locale, modifier: Modifier = Modifier) {
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current

    LocalizedName(
        name = remember(context, locale, appLanguage) { locale.getDisplayName(context, inLocale = locale) },
        secondName = remember(context, locale) { locale.getDisplayName(context, inLocale = appLanguage) },
        modifier = modifier,
    )
}

@Composable
internal fun LanguageName(language: Language, modifier: Modifier = Modifier) {
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current

    LocalizedName(
        name = remember(context, language) { language.getDisplayName(context, language.code) },
        secondName = remember(context, language, appLanguage) { language.getDisplayName(context, appLanguage) },
        modifier = modifier,
    )
}
