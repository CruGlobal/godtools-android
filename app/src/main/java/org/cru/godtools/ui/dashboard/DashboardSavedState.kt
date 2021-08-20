package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.cru.godtools.base.ui.dashboard.Page

private const val ATTR_SELECTED_PAGE = "selectedPage"
private val DEFAULT_PAGE = Page.FAVORITE_TOOLS

internal class DashboardSavedState(private val savedState: SavedStateHandle) : ViewModel() {
    var selectedPage: Page
        get() = savedState[ATTR_SELECTED_PAGE] ?: DEFAULT_PAGE
        set(value) { savedState[ATTR_SELECTED_PAGE] = value }

    val selectedPageLiveData get() = savedState.getLiveData(ATTR_SELECTED_PAGE, DEFAULT_PAGE)
}
