package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus

private const val KEY_PAGE_STACK = "pageStack"

@HiltViewModel
class DashboardViewModel @Inject constructor(private val savedState: SavedStateHandle) : ViewModel() {
    // region Page Stack
    private var pageStack: List<Page>
        get() = savedState.get<List<Page>>(KEY_PAGE_STACK)?.toList() ?: listOf(Page.DEFAULT)
        set(value) = savedState.set(KEY_PAGE_STACK, ArrayList(value))
    private val pageStackFlow = savedState.getStateFlow(KEY_PAGE_STACK, listOf(Page.DEFAULT))

    val currentPage = pageStackFlow
        .map { it.lastOrNull() ?: Page.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Page.DEFAULT)

    fun updateCurrentPage(page: Page, clearStack: Boolean = true) {
        pageStack = if (clearStack) listOf(page) else pageStack + page
    }
    // endregion Page Stack

    // region optInNotification logic
    var permissionStatus: PermissionStatus? = null
        private set

    fun setPermissionStatus(status: PermissionStatus) {
        permissionStatus = status
    }

    private val _showOptInNotification = MutableStateFlow(false)
    val showOptInNotification = _showOptInNotification.asStateFlow()

    fun setShowOptInNotification(bool: Boolean) {
        _showOptInNotification.value = bool
    }
    // endregion optInNotification logic
}
