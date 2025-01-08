package org.cru.godtools.tool.cyoa.ui

import androidx.core.graphics.Insets
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.delegate

private const val LEFT = "left"
private const val TOP = "top"
private const val RIGHT = "right"
private const val BOTTOM = "bottom"

private const val DEFAULT_LEFT = 0
private const val DEFAULT_TOP = 0
private const val DEFAULT_RIGHT = 0
private const val DEFAULT_BOTTOM = 0

class PageInsets(savedState: SavedStateHandle) : ViewModel() {
    var left by savedState.delegate(LEFT, ifNull = DEFAULT_LEFT)
    var top by savedState.delegate(TOP, ifNull = DEFAULT_TOP)
    var right by savedState.delegate(RIGHT, ifNull = DEFAULT_RIGHT)
    var bottom by savedState.delegate(BOTTOM, ifNull = DEFAULT_BOTTOM)

    val insets = combine(
        savedState.getStateFlow(LEFT, DEFAULT_LEFT),
        savedState.getStateFlow(TOP, DEFAULT_TOP),
        savedState.getStateFlow(RIGHT, DEFAULT_RIGHT),
        savedState.getStateFlow(BOTTOM, DEFAULT_BOTTOM)
    ) { l, t, r, b -> Insets.of(l, t, r, b) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Insets.NONE)
}
