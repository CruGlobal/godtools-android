package org.cru.godtools.ui.dashboard.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.keynote.godtools.android.db.repository.LessonsRepository

@HiltViewModel
class LessonsViewModel @Inject constructor(lessonsRepository: LessonsRepository) : ViewModel() {
    val lessons = lessonsRepository.lessons
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
