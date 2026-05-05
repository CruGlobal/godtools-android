package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.ToolCard.UiState

interface ToolCardPresenter {
    @Composable
    fun present(
        tool: Tool,
        customLocale: Locale? = null,
        loadAppLanguage: Boolean = false,
        secondLanguage: Language? = null,
        loadAvailableLanguages: Boolean = false,
        eventSink: (ToolCard.UiEvent) -> Unit = {},
    ): UiState
}
