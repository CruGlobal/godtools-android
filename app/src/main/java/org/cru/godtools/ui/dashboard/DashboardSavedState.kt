package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

private const val ATTR_SELECTED_PAGE = "selectedPage"

internal class DashboardSavedState(private val savedState: SavedStateHandle) : ViewModel() {
    var selectedPage: Page
        get() = savedState[ATTR_SELECTED_PAGE] ?: Page.DEFAULT
        set(value) { savedState[ATTR_SELECTED_PAGE] = value }
}
