package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.sync.GodToolsSyncService

private const val KEY_PAGE_STACK = "pageStack"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val syncService: GodToolsSyncService,
    private val savedState: SavedStateHandle
) : ViewModel() {
    // region Page Stack
    private var pageStack: List<Page>
        get() = savedState.get<List<Page>>(KEY_PAGE_STACK)?.toList() ?: listOf(Page.DEFAULT)
        set(value) { savedState[KEY_PAGE_STACK] = ArrayList(value) }
    private val pageStackFlow = savedState.getStateFlow(KEY_PAGE_STACK, listOf(Page.DEFAULT))

    val hasBackStack = pageStackFlow
        .map { it.size > 1 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentPage = pageStackFlow
        .map { it.lastOrNull() ?: Page.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Page.DEFAULT)

    fun updateCurrentPage(page: Page, clearStack: Boolean = true) {
        pageStack = if (clearStack) listOf(page) else pageStack + page
    }

    fun popPageStack() {
        pageStack = pageStack.dropLast(1)
    }
    // endregion Page Stack

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            @Suppress("DeferredResultUnused")
            syncService.syncFollowupsAsync()
            @Suppress("DeferredResultUnused")
            syncService.syncToolSharesAsync()
            syncsRunning.value++
            coroutineScope {
                launch { syncService.syncFavoriteTools(force) }
                launch { syncService.syncTools(force) }
            }
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
