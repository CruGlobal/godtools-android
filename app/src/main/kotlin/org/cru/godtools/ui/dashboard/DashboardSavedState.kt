package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.ccci.gto.android.common.androidx.lifecycle.delegate
import org.ccci.gto.android.common.androidx.lifecycle.livedata
import org.cru.godtools.base.ui.dashboard.Page

private const val ATTR_SELECTED_PAGE = "selectedPage"
private val DEFAULT_PAGE = Page.FAVORITE_TOOLS

internal class DashboardSavedState(savedState: SavedStateHandle) : ViewModel() {
    var selectedPage by savedState.delegate(ATTR_SELECTED_PAGE, DEFAULT_PAGE)
    val selectedPageLiveData by savedState.livedata(ATTR_SELECTED_PAGE, DEFAULT_PAGE)
}
