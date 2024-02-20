package org.cru.godtools.ui.tooldetails

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.io.File
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : Screen {
    data class State(
        val toolCode: String? = null,
        val tool: Tool? = null,
        val banner: File? = null,
        val bannerAnimation: File? = null,
        val translation: Translation? = null,
        val availableLanguages: ImmutableList<String> = persistentListOf(),
        val variants: List<ToolCard.State> = emptyList(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class SwitchVariant(val variant: String) : Event
    }
}
