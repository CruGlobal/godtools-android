package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import java.io.File
import java.util.Locale
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

interface ToolCardPresenter {
    @Composable
    fun present(
        tool: Tool,
        customLocale: Locale? = null,
        loadAppLanguage: Boolean = false,
        secondLanguage: Language? = null,
        loadAvailableLanguages: Boolean = false,
        eventSink: (UiEvent) -> Unit = {},
    ): UiState

    data class UiState(
        val toolCode: String? = null,
        val tool: Tool? = null,
        val isLoaded: Boolean = true,
        val banner: File? = null,
        val language: Language? = null,
        val languageAvailable: Boolean = false,
        val translation: Translation? = null,
        val appLanguage: Language? = null,
        val appLanguageAvailable: Boolean = false,
        val secondLanguage: Language? = null,
        val secondLanguageAvailable: Boolean = false,
        val progress: Progress? = null,
        val availableLanguages: Int = 0,
        val downloadProgress: DownloadProgress? = null,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState {
        sealed interface Progress {
            val progress: Double

            @JvmInline
            value class InProgress(override val progress: Double) : Progress
            data object Completed : Progress {
                override val progress = 1.0
            }
        }
    }

    sealed interface UiEvent : CircuitUiEvent {
        data object Click : UiEvent
        data object OpenTool : UiEvent
        data object OpenToolDetails : UiEvent
        data object PinTool : UiEvent
        data object UnpinTool : UiEvent
    }
}
