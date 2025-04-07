package org.cru.godtools.ui.languages.downloadable

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language

@Parcelize
object DownloadableLanguagesScreen : Screen {
    data class UiState(val languages: ImmutableList<UiLanguage>) : CircuitUiState {
        data class UiLanguage(
            val language: Language,
            val downloadedTools: Int,
            val totalTools: Int,
            val eventSink: (UiEvent) -> Unit,
        ) : CircuitUiState {
            sealed interface UiEvent : CircuitUiEvent {
                data object PinLanguage : UiEvent
                data object UnpinLanguage : UiEvent
            }
        }
    }
}
