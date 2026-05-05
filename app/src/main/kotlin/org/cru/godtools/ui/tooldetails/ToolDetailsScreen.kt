package org.cru.godtools.ui.tooldetails

import androidx.annotation.StringRes
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.io.File
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.R
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.drawer.DrawerMenuScreen
import org.cru.godtools.ui.tools.ToolCardPresenter

@Parcelize
data class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : Screen {
    data class UiState(
        val toolCode: String? = null,
        val tool: Tool? = null,
        val banner: File? = null,
        val bannerAnimation: File? = null,
        val downloadProgress: DownloadProgress? = null,
        val hasShortcut: Boolean = false,
        val hasTips: Boolean = false,
        val translation: Translation? = null,
        val secondTranslation: Translation? = null,
        val secondLanguage: Language? = null,
        val pages: ImmutableList<Page> = persistentListOf(Page.DESCRIPTION),
        val availableLanguages: ImmutableList<String> = persistentListOf(),
        val variants: List<ToolCardPresenter.UiState> = listOf(),
        val drawerState: DrawerMenuScreen.State = DrawerMenuScreen.State(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    enum class Page(@StringRes val tabLabel: Int) {
        DESCRIPTION(R.string.label_tools_about),
        VARIANTS(R.string.tool_details_section_variants_label)
    }

    sealed interface UiEvent : CircuitUiEvent {
        data object NavigateUp : UiEvent
        data object OpenTool : UiEvent
        data object OpenToolTraining : UiEvent
        data object PinTool : UiEvent
        data object UnpinTool : UiEvent
        data object PinShortcut : UiEvent
    }
}
