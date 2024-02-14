package org.cru.godtools.ui.languages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

@Composable
@Preview(locale = "en", showBackground = true)
private fun LeftToRightLanguageNames() = Column {
    LanguageName(Locale("ar"))
    HorizontalDivider()
    LanguageName(Locale.forLanguageTag("bs-BA"))
    HorizontalDivider()
    LanguageName(Locale.ENGLISH)
}

@Composable
@Preview(locale = "ar", showBackground = true)
private fun RightToLeftLanguageNames() = Column {
    LanguageName(Locale("ar"))
    HorizontalDivider()
    LanguageName(Locale.forLanguageTag("bs-BA"))
    HorizontalDivider()
    LanguageName(Locale.ENGLISH)
}
