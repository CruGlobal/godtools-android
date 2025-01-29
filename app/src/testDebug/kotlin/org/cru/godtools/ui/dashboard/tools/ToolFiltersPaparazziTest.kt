package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.dashboard.filters.FilterMenu.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ToolFiltersPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `LanguageFilter - Button - No Language Selected`() = renderLanguageFilter(
        UiState(
            selectedItem = null,
            menuExpanded = mutableStateOf(false),
        )
    )

    @Test
    fun `LanguageFilter - Button - English Selected`() = renderLanguageFilter(
        UiState(
            selectedItem = Language(Locale.ENGLISH),
            menuExpanded = mutableStateOf(false),
        )
    )

    // TODO: It appears that LayoutLib does not correctly support Popups/Windows currently
    //       see: https://issuetracker.google.com/issues/317792376
    //       see: https://issuetracker.google.com/issues/308808808
    //       see: https://issuetracker.google.com/issues/321623569
    @Test
    @Ignore("Ignored for now due to LayoutLib rendering issues")
    fun `LanguageFilter - Dropdown Menu`() = renderLanguageFilter(
        UiState(
            selectedItem = Language(Locale.ENGLISH),
            menuExpanded = mutableStateOf(true),
            items = persistentListOf(
                UiState.Item(Language(Locale.ENGLISH), 12345),
                UiState.Item(Language(Locale.FRENCH), 1),
                UiState.Item(Language(Locale("es")), 3),
            ),
        )
    )

    private fun renderLanguageFilter(state: UiState<Language>) = centerInSnapshot {
        LanguageFilter(state, modifier = Modifier.fillMaxWidth(0.5f))
    }
}
