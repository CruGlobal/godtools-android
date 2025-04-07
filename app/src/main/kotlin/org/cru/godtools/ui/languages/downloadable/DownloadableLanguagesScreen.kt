package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language

@Parcelize
object DownloadableLanguagesScreen : Screen {
    data class UiState(
        val query: MutableState<String> = mutableStateOf(""),
        val languages: ImmutableList<UiLanguage> = persistentListOf(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState {
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

        sealed interface UiEvent : CircuitUiEvent {
            data object NavigateUp : UiEvent
        }
    }
}
