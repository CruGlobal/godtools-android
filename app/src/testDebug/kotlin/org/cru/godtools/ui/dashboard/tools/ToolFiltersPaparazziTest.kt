package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Ignore
import kotlin.test.Test
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ToolFiltersPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `LanguageFilter - Button - No Language Selected`() = renderLanguageFilter(
        ToolsScreen.Filters(
            selectedLanguage = null,
            showLanguagesMenu = false,
        )
    )

    @Test
    fun `LanguageFilter - Button - English Selected`() = renderLanguageFilter(
        ToolsScreen.Filters(
            selectedLanguage = Language(Locale.ENGLISH),
            showLanguagesMenu = false,
        )
    )

    // TODO: It appears that LayoutLib does not correctly support Popups/Windows currently
    //       see: https://issuetracker.google.com/issues/317792376
    //       see: https://issuetracker.google.com/issues/308808808
    //       see: https://issuetracker.google.com/issues/321623569
    @Test
    @Ignore("Ignored for now due to LayoutLib rendering issues")
    fun `LanguageFilter - Dropdown Menu`() = renderLanguageFilter(
        ToolsScreen.Filters(
            selectedLanguage = Language(Locale.ENGLISH),
            showLanguagesMenu = true,
            languages = listOf(
                ToolsScreen.Filters.Filter(Language(Locale.ENGLISH), 12345),
                ToolsScreen.Filters.Filter(Language(Locale.FRENCH), 1),
                ToolsScreen.Filters.Filter(Language(Locale("es")), 3),
            ),
        )
    )

    private fun renderLanguageFilter(filters: ToolsScreen.Filters) = centerInSnapshot {
        LanguageFilter(filters, modifier = Modifier.fillMaxWidth(0.5f))
    }
}
