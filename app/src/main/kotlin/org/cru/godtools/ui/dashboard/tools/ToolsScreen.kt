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
    ) : CircuitUiState {
        data class Filters(
            val categories: List<String> = emptyList(),
            val selectedCategory: String? = null,
            val languages: List<Language> = emptyList(),
            val languageQuery: String = "",
            val selectedLanguage: Language? = null,
        )
    }

    sealed interface Event : CircuitUiEvent {
        data class OpenToolDetails(val tool: String, val source: String? = null) : Event
        data class UpdateSelectedCategory(val category: String?) : Event
        data class UpdateLanguageQuery(val query: String) : Event
        data class UpdateSelectedLanguage(val locale: Locale?) : Event
    }
}
