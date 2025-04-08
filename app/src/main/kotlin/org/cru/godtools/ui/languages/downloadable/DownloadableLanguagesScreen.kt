package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
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
        data class UiLanguage(val language: Language, val downloadedTools: Int = 0, val totalTools: Int = 0)

        sealed interface UiEvent : CircuitUiEvent {
            data object NavigateUp : UiEvent
            data class PinLanguage(val locale: Locale) : UiEvent
            data class UnpinLanguage(val locale: Locale) : UiEvent
        }
    }
}
