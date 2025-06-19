package org.cru.godtools.base.tool.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.shared.renderer.state.State
import org.greenrobot.eventbus.EventBus

private const val STATE_TOOL_STATE = "toolState"

@HiltViewModel
class ToolStateHolder @Inject constructor(private val eventBus: EventBus, savedState: SavedStateHandle) : ViewModel() {
    val toolState = savedState[STATE_TOOL_STATE] ?: State().also { savedState[STATE_TOOL_STATE] = it }

    init {
        // TODO: temporarily pipe content events into EventBus
        toolState.contentEvents
            .onEach { eventBus.post(Event.Builder().id(it).build()) }
            .launchIn(viewModelScope)
    }
}
