package org.cru.godtools.ui.tooldetails

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.io.File
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : Screen {
    data class State(
        val toolCode: String? = null,
        val tool: Tool? = null,
        val banner: File? = null,
        val bannerAnimation: File? = null,
        val downloadProgress: DownloadProgress? = null,
        val hasShortcut: Boolean = false,
        val translation: Translation? = null,
        val secondTranslation: Translation? = null,
        val secondLanguage: Language? = null,
        val manifest: Manifest? = null,
        val pages: ImmutableList<ToolDetailsPage> = persistentListOf(ToolDetailsPage.DESCRIPTION),
        val availableLanguages: ImmutableList<String> = persistentListOf(),
        val variants: ImmutableList<ToolCard.State> = persistentListOf(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object NavigateUp : Event
        data object OpenTool : Event
        data object OpenToolTraining : Event
        data object PinTool : Event
        data object UnpinTool : Event
        data class SwitchVariant(val variant: String) : Event
        data object PinShortcut : Event
    }
}
