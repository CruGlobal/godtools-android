package org.cru.godtools.ui.dashboard.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_LESSONS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.dashboard.Page
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
    fun trackPageInAnalytics(page: Page) = when (page) {
        Page.LESSONS -> {
            eventBus.post(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_LESSONS))
            eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS))
        }
        else -> Unit
    }
    // endregion Analytics
}
