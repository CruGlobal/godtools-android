package org.cru.godtools.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.user.data.UserManager

@HiltViewModel
class AccountViewModel @Inject internal constructor(
    private val syncService: GodToolsSyncService,
    userManager: UserManager
) : ViewModel() {
    val user = userManager.userFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val pages = MutableStateFlow(listOf(AccountPage.GLOBAL_ACTIVITY))

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            syncsRunning.value++
            coroutineScope {
                launch { syncService.syncUser(force) }
                launch { syncService.syncGlobalActivity(force) }
            }
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
