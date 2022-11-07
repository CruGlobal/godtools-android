package org.cru.godtools.ui.account.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.user.activity.UserActivityManager

@HiltViewModel
class AccountActivityViewModel @Inject constructor(
    userActivityManager: UserActivityManager,
) : ViewModel() {
    val userActivity = userActivityManager.userActivityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UserActivity(emptyMap()))
}
