package org.cru.godtools.ui.tools

import io.mockk.MockKAnswerScope
import java.util.Locale
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.ToolCard.Event

fun MockKAnswerScope<ToolCard.State, ToolCard.State>.toolArg() = arg<Tool>(0)
fun MockKAnswerScope<ToolCard.State, ToolCard.State>.customLocaleArg() = arg<Locale?>(1)
fun MockKAnswerScope<ToolCard.State, ToolCard.State>.eventSinkArg() = arg<(Event) -> Unit>(5)
