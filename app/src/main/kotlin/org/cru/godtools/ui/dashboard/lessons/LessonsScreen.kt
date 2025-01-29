package org.cru.godtools.ui.dashboard.lessons

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language
import org.cru.godtools.ui.dashboard.filters.FilterMenu

@Parcelize
data object LessonsScreen : Screen {
    data class UiState(val languageFilter: FilterMenu.UiState<Language> = FilterMenu.UiState()) : CircuitUiState
}
