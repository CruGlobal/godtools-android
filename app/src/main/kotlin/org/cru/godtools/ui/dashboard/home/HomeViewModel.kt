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
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.ui.banner.BannerType
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.repository.LessonsRepository
import org.keynote.godtools.android.db.repository.ToolsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val eventBus: EventBus,
    lessonsRepository: LessonsRepository,
    settings: Settings,
    private val toolsRepository: ToolsRepository
) : ViewModel() {
    val banner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES)
        .map { featureTutorial ->
            when {
                !featureTutorial && PageSet.FEATURES.supportsLocale(Locale.getDefault()) -> BannerType.TUTORIAL_FEATURES
                else -> null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val spotlightLessons = lessonsRepository.spotlightLessons
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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

    // region Analytics
    fun recordOpenClickInAnalytics(action: String, tool: String?, source: String) {
        eventBus.post(OpenAnalyticsActionEvent(action, tool, source))
    }
    // endregion Analytics
}
