package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_HOME
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_LESSONS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus

private const val KEY_PAGE_STACK = "pageStack"
private val DEFAULT_PAGE = Page.HOME

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val eventBus: EventBus,
    private val syncService: GodToolsSyncService,
    private val savedState: SavedStateHandle
) : ViewModel() {
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

    // region Analytics
    fun trackPageInAnalytics(page: Page) = when (page) {
        Page.LESSONS -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_LESSONS))
            eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS))
        }
        Page.HOME, Page.FAVORITE_TOOLS -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_HOME))
            eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME))
        }
        Page.ALL_TOOLS -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_ALL_TOOLS))
            eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS))
        }
        else -> Unit
    }
    // endregion Analytics

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            launch { syncService.executeSyncTask(syncService.syncFollowups()) }
            syncsRunning.value++
            syncService.syncTools(force)
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
