package org.cru.godtools.ui.dashboard.tools

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
data object ToolsScreen : Screen {
    data class State(
        val banner: BannerType? = null,
        val spotlightTools: List<ToolCard.State> = emptyList(),
        val filters: Filters = Filters(),
        val tools: List<Tool> = emptyList(),
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    data class Filters(
        val categories: List<String> = emptyList(),
        val selectedCategory: String? = null,
        val languages: List<Language> = emptyList(),
        val languageQuery: String = "",
        val selectedLanguage: Language? = null,
        val eventSink: (FiltersEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class OpenToolDetails(val tool: String, val source: String? = null) : Event
    }

    sealed interface FiltersEvent : CircuitUiEvent {
        data class UpdateLanguageQuery(val query: String) : FiltersEvent
        data class SelectCategory(val category: String?) : FiltersEvent
        data class SelectLanguage(val locale: Locale?) : FiltersEvent
    }
}
