package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.sync.GodToolsSyncService

private const val KEY_PAGE_STACK = "pageStack"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val syncService: GodToolsSyncService,
    private val savedState: SavedStateHandle,
    private val settings: Settings,
) : ViewModel() {
    // region Page Stack
    private var pageStack: List<Page>
        get() = savedState.get<List<Page>>(KEY_PAGE_STACK)?.toList() ?: listOf(Page.DEFAULT)
        set(value) = savedState.set(KEY_PAGE_STACK, ArrayList(value))
    private val pageStackFlow = savedState.getStateFlow(KEY_PAGE_STACK, listOf(Page.DEFAULT))

    val hasBackStack = pageStackFlow.map { it.size > 1 }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentPage = pageStackFlow.map { it.lastOrNull() ?: Page.DEFAULT }
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
            @Suppress("DeferredResultUnused") syncService.syncFollowupsAsync()
            @Suppress("DeferredResultUnused") syncService.syncToolSharesAsync()
            syncsRunning.value++
            coroutineScope {
                launch { syncService.syncFavoriteTools(force) }
                launch { syncService.syncTools(force) }
            }
            syncsRunning.value--
        }
    }

    // endregion Sync logic

    // region optInNotification logic
    private val _permissionStatus = MutableStateFlow<PermissionStatus?>(null)
    val permissionStatus: StateFlow<PermissionStatus?> = _permissionStatus

    fun setPermissionStatus(status: PermissionStatus) {
        _permissionStatus.value = status
    }

    private val _isOptInNotificationActive = MutableStateFlow(false)
    val isOptInNotificationActive: StateFlow<Boolean> = _isOptInNotificationActive

    fun setIsOptInNotificationActive(bool: Boolean) {
        _isOptInNotificationActive.value = bool
    }

    private val _showNotificationSettingsDialog = MutableStateFlow(false)
    val showNotificationSettingsDialog: StateFlow<Boolean> = _showNotificationSettingsDialog

    fun setShowNotificationSettingsDialog(bool: Boolean) {
        _showNotificationSettingsDialog.value = bool
    }

    fun shouldPromptNotificationSheet() {

        // ensure notification status is refreshed


        val lastPrompted = settings.getLastPromptedOptInNotification() ?: Date(Long.MIN_VALUE)
        val promptCount = settings.getOptInNotificationPromptCount()

        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val lastPromptedTestDate = dateFormat.parse("01/01/2020") ?: Date()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -2)
        val twoMonthsAgo = calendar.time

        if (settings.launches == 1) {
            println("Returning due to onboarding launch")
            return}
        if (permissionStatus.value == PermissionStatus.APPROVED) {
            println("Returning due to approved permission status")
            return}
        // TODO - DSR: update prompt count value
        if (promptCount > 1000) {
            println("Returning due to prompt count")
            return}

        // return if isOnboardingLaunch, notification permission is already granted, or promptCount exceeds maxPrompts

        if (lastPromptedTestDate < twoMonthsAgo) {
            _isOptInNotificationActive.value = true
            settings.recordOptInNotificationPrompt()
        }
    }
    // endregion optInNotification logic

    init {
        triggerSync()
    }
}
