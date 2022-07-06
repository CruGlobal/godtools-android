package org.cru.godtools.ui.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_HOME
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_HOME
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.tutorial.PageSet
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.repository.LessonsRepository
import org.keynote.godtools.android.db.repository.ToolsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val eventBus: EventBus,
    lessonsRepository: LessonsRepository,
    settings: Settings,
    private val syncService: GodToolsSyncService,
    private val toolsRepository: ToolsRepository
) : ViewModel() {
    val showTutorialFeaturesBanner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES)
        .map { !it && PageSet.FEATURES.supportsLocale(Locale.getDefault()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val spotlightLessons = lessonsRepository.spotlightLessons
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // region Analytics
    fun trackPageInAnalytics(page: Page) = when (page) {
        Page.HOME, Page.FAVORITE_TOOLS -> {
            eventBus.post(AnalyticsScreenEvent(SCREEN_HOME))
            eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME))
        }
        else -> Unit
    }
    // endregion Analytics

    // region Favorites Tools
    val favoriteTools = toolsRepository.favoriteTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val favoriteToolsOrder = MutableStateFlow(emptyList<Tool>())
    val reorderableFavoriteTools = favoriteTools
        .flatMapLatest {
            favoriteToolsOrder.value = it.orEmpty()
            favoriteToolsOrder
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun moveFavoriteTool(from: Int, to: Int) {
        favoriteToolsOrder.value = favoriteToolsOrder.value.toMutableList().apply { add(to, removeAt(from)) }
    }

    fun commitFavoriteToolOrder() {
        viewModelScope.launch { toolsRepository.updateToolOrder(favoriteToolsOrder.value.mapNotNull { it.code }) }
    }
    // endregion Favorite Tools

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            syncsRunning.value++
            syncService.suspendAndSyncTools(force)
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
