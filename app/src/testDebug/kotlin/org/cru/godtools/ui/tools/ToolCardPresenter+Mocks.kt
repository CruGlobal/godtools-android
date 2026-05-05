package org.cru.godtools.ui.tools

import io.mockk.MockKAnswerScope
import java.util.Locale
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.ToolCard.UiEvent

fun MockKAnswerScope<ToolCard.UiState, ToolCard.UiState>.toolArg() = arg<Tool>(0)
fun MockKAnswerScope<ToolCard.UiState, ToolCard.UiState>.customLocaleArg() = arg<Locale?>(1)
fun MockKAnswerScope<ToolCard.UiState, ToolCard.UiState>.eventSinkArg() = arg<(UiEvent) -> Unit>(5)
