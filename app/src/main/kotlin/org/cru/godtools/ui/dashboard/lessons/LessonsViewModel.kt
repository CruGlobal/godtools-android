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
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.repository.LessonsRepository

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val eventBus: EventBus,
    lessonsRepository: LessonsRepository,
) : ViewModel() {
    val lessons = lessonsRepository.lessons
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // region Analytics
    fun recordOpenLessonInAnalytics(tool: String?) {
        eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool, SOURCE_LESSONS))
    }
    // endregion Analytics
}
