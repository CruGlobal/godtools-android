package org.cru.godtools.ui.tooldetails

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

@Parcelize
class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : Screen {
    data class State(
        val tool: Tool? = null,
        val translation: Translation? = null,
        val availableLanguages: ImmutableList<String> = persistentListOf(),
    ) : CircuitUiState
}
