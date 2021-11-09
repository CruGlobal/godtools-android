package org.cru.godtools.tool.cyoa.ui

import androidx.core.graphics.Insets
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import org.ccci.gto.android.common.androidx.lifecycle.combine
import org.ccci.gto.android.common.androidx.lifecycle.delegate

private const val LEFT = "left"
private const val TOP = "left"
private const val RIGHT = "left"
private const val BOTTOM = "left"

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
        savedState.getLiveData(LEFT, DEFAULT_LEFT).distinctUntilChanged(),
        savedState.getLiveData(TOP, DEFAULT_TOP).distinctUntilChanged(),
        savedState.getLiveData(RIGHT, DEFAULT_RIGHT).distinctUntilChanged(),
        savedState.getLiveData(BOTTOM, DEFAULT_BOTTOM).distinctUntilChanged()
    ) { l, t, r, b -> Insets.of(l, t, r, b) }
}
