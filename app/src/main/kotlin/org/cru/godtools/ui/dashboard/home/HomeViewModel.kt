package org.cru.godtools.ui.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.ui.banner.BannerType
import org.greenrobot.eventbus.EventBus

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventBus: EventBus,
    settings: Settings,
    toolsRepository: ToolsRepository
) : ViewModel() {
    val banner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES)
        .map { featureTutorial ->
            when {
                !featureTutorial && PageSet.FEATURES.supportsLocale(Locale.getDefault()) -> BannerType.TUTORIAL_FEATURES
                else -> null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val spotlightLessons = toolsRepository.getLessonsFlow()
        .map { it.filter { !it.isHidden && it.isSpotlight }.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val favoriteTools = toolsRepository.getFavoriteToolsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // region Analytics
    fun recordOpenClickInAnalytics(action: String, tool: String?, source: String) {
        eventBus.post(OpenAnalyticsActionEvent(action, tool, source))
    }
    // endregion Analytics
}
