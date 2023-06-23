package org.cru.godtools.ui.languages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

@Composable
@Preview(locale = "en", showBackground = true)
fun LeftToRightLanguageNames() = Column {
    LanguageName(Locale("ar"))
    Divider()
    LanguageName(Locale.forLanguageTag("bs-BA"))
    Divider()
    LanguageName(Locale.ENGLISH)
}

@Composable
@Preview(locale = "ar", showBackground = true)
fun RightToLeftLanguageNames() = Column {
    LanguageName(Locale("ar"))
    Divider()
    LanguageName(Locale.forLanguageTag("bs-BA"))
    Divider()
    LanguageName(Locale.ENGLISH)
}
