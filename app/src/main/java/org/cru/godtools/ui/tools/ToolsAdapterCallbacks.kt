package org.cru.godtools.ui.tools

import org.cru.godtools.model.Tool
import java.util.Locale

interface ToolsAdapterCallbacks {
    fun onToolInfo(code: String?)
    fun onToolSelect(code: String?, type: Tool.Type, vararg languages: Locale?)
    fun onToolAdd(code: String?)
    fun onToolsReordered(vararg ids: Long)
}
