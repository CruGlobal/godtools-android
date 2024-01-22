package org.cru.godtools.ui.dashboard.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_LESSONS
import org.cru.godtools.db.repository.ToolsRepository
import org.greenrobot.eventbus.EventBus

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val eventBus: EventBus,
    toolsRepository: ToolsRepository,
) : ViewModel() {
    val lessons = toolsRepository.getLessonsFlow()
        .map { it.filterNot { it.isHidden }.sortedBy { it.defaultOrder }.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // region Analytics
    fun recordOpenLessonInAnalytics(tool: String?) {
        eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool, SOURCE_LESSONS))
    }
    // endregion Analytics
}
