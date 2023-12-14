package org.cru.godtools.ui.dashboard.tools

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language

@Parcelize
data object ToolsScreen : Screen {
    data class State(
        val filters: Filters = Filters(),
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState {
        data class Filters(
            val languages: List<Language> = emptyList(),
            val languageQuery: String = "",
            val selectedLanguage: Language? = null,
        )
    }

    sealed interface Event : CircuitUiEvent {
        data class UpdateLanguageQuery(val query: String) : Event
        data class UpdateSelectedLanguage(val locale: Locale?) : Event
    }
}
