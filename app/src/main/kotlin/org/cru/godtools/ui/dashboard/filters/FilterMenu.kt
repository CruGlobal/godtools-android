package org.cru.godtools.ui.dashboard.filters

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FilterMenu {
    data class UiState<T>(
        val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
        val query: MutableState<String> = mutableStateOf(""),
        val items: ImmutableList<Item<T>> = persistentListOf(),
        val selectedItem: T? = null,
        val eventSink: (Event<T>) -> Unit = {},
    ) : CircuitUiState {
        data class Item<T>(val item: T, val count: Int)
    }

    sealed interface Event<T> : CircuitUiEvent {
        data class SelectItem<T>(val item: T?) : Event<T>
    }
}
