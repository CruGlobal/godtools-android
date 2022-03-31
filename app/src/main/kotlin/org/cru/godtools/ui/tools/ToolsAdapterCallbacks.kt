package org.cru.godtools.ui.tools

import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

interface ToolsAdapterCallbacks {
    fun onToolInfo(code: String?)
    fun openTool(tool: Tool?, primary: Translation?) = openTool(tool, primary, null)
    fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?)
    fun addTool(code: String?)
    fun removeTool(tool: Tool?, translation: Translation?)
    fun onToolsReordered(vararg ids: Long)
}
