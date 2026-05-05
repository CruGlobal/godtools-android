package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTranslation

class FakeToolCardPresenter(
    var onPresent: (Tool, Locale?, (ToolCard.UiEvent) -> Unit) -> ToolCard.UiState = { tool, customLocale, eventSink ->
        ToolCard.UiState(
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
        eventSink: (ToolCard.UiEvent) -> Unit,
    ) = onPresent(tool, customLocale, eventSink)
}
