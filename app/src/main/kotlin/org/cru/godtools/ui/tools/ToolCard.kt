package org.cru.godtools.ui.tools

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import java.io.File
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

object ToolCard {
    data class State(
        val tool: Tool? = null,
        val banner: File? = null,
        val translation: Translation? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object PinTool : Event
        data object UnpinTool : Event
    }
}
