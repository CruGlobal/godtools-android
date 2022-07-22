package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.base.ui.dashboard.Page

private const val KEY_PAGE_STACK = "pageStack"
private val DEFAULT_PAGE = Page.HOME

class DashboardViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    // region Page Stack
    private var pageStack: List<Page>
        get() = savedState.get<ArrayList<Page>>(KEY_PAGE_STACK)?.toList()
            ?: listOf(DEFAULT_PAGE)
        set(value) {
            savedState[KEY_PAGE_STACK] = ArrayList(value)
        }
    private val pageStackFlow = savedState.getStateFlow(KEY_PAGE_STACK, listOf(DEFAULT_PAGE))

    val hasBackStack = pageStackFlow
        .map { it.size > 1 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentPage = pageStackFlow
        .map { it.lastOrNull() ?: DEFAULT_PAGE }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_PAGE)

    fun updateCurrentPage(page: Page, clearStack: Boolean = true) {
        pageStack = if (clearStack) listOf(page) else pageStack + page
    }
    fun popPageStack() {
        pageStack = pageStack.dropLast(1)
    }
    // endregion Page Stack
}
