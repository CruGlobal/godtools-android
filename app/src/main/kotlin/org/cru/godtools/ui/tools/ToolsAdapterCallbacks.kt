package org.cru.godtools.ui.tools

import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

interface ToolsAdapterCallbacks {
    fun onToolClicked(tool: Tool?, primary: Translation?) = onToolClicked(tool, primary, null)
    fun onToolClicked(tool: Tool?, primary: Translation?, parallel: Translation?) = openTool(tool, primary, parallel)

    fun openTool(tool: Tool?, primary: Translation?) = openTool(tool, primary, null)
    fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?)

    fun showToolDetails(code: String?)

    fun pinTool(code: String?)
    fun unpinTool(tool: Tool?, translation: Translation?)

    fun onToolsReordered(vararg ids: Long) = Unit
}
