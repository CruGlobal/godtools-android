package org.cru.godtools.ui.banner

import com.slack.circuit.runtime.CircuitUiState

object Banner {
    enum class Type { TOOL_LIST_FAVORITES, TUTORIAL_FEATURES }

    interface UiState : CircuitUiState {
        val type: Type
    }
}
