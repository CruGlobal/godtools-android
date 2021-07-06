package org.cru.godtools.base.tool.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.cru.godtools.tool.state.State

private const val STATE_TOOL_STATE = "toolState"

class ToolStateHolder(private val savedState: SavedStateHandle) : ViewModel() {
    val toolState by lazy {
        savedState[STATE_TOOL_STATE] ?: State().also { savedState[STATE_TOOL_STATE] = it }
    }
}
