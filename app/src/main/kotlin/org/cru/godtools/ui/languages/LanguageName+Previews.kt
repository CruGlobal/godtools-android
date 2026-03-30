package org.cru.godtools.ui.languages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import org.cru.godtools.model.Language

@Composable
@Preview(locale = "en", showBackground = true)
private fun LeftToRightLanguageNames() = Column {
    LocalizedName(Locale("ar"))
    HorizontalDivider()
    LocalizedName(Locale.forLanguageTag("bs-BA"))
    HorizontalDivider()
    LocalizedName(Locale.ENGLISH)
    HorizontalDivider()
    LocalizedName(language = nameLanguage)
    HorizontalDivider()
    LocalizedName(language = forcedNameLanguage)
}

@Composable
@Preview(locale = "ar", showBackground = true)
private fun RightToLeftLanguageNames() = Column {
    LocalizedName(Locale("ar"))
    HorizontalDivider()
    LocalizedName(Locale.forLanguageTag("bs-BA"))
    HorizontalDivider()
    LocalizedName(Locale.ENGLISH)
    HorizontalDivider()
    LocalizedName(language = nameLanguage)
    HorizontalDivider()
    LocalizedName(language = forcedNameLanguage)
}

private val forcedNameLanguage = Language(
    code = Locale.ENGLISH,
    name = "German",
    isForcedName = true,
)

private val nameLanguage = Language(
    code = Locale.UK,
    name = "German",
    isForcedName = false,
)
