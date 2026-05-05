package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.tools.ToolCardPresenter.UiEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState

class FakeToolCardPresenter(
    var onPresent: (Tool, Locale?, (UiEvent) -> Unit) -> UiState = { tool, customLocale, eventSink ->
        UiState(
            tool = tool,
            toolCode = tool.code,
            translation = randomTranslation(toolCode = tool.code, languageCode = customLocale ?: Locale.ENGLISH),
            eventSink = eventSink
        )
    },
) : ToolCardPresenter {
    @Composable
    override fun present(
        tool: Tool,
        customLocale: Locale?,
        loadAppLanguage: Boolean,
        secondLanguage: Language?,
        loadAvailableLanguages: Boolean,
        eventSink: (UiEvent) -> Unit,
    ) = onPresent(tool, customLocale, eventSink)
}
