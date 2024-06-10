package org.cru.godtools.ui.account.globalactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.model.GlobalActivityAnalytics

@HiltViewModel
class GlobalActivityViewModel @Inject constructor(globalActivityRepository: GlobalActivityRepository) : ViewModel() {
    val activity = globalActivityRepository.getGlobalActivityFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), GlobalActivityAnalytics())
}
