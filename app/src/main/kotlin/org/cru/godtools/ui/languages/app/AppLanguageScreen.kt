package org.cru.godtools.ui.languages.app

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
data object AppLanguageScreen : Screen {
    data class State(
        val languages: List<Locale> = emptyList(),
        val languageQuery: String = "",
        val selectedLanguage: Locale? = null,
        val eventSink: (Event) -> Unit = {}
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object NavigateBack : Event
        data class UpdateLanguageQuery(val query: String) : Event
        data class SelectLanguage(val language: Locale) : Event
        data class ConfirmLanguage(val language: Locale) : Event
        data object DismissConfirmDialog : Event
    }
}
