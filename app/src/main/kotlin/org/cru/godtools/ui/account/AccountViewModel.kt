package org.cru.godtools.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.sync.GodToolsSyncService

@HiltViewModel
class AccountViewModel @Inject internal constructor(
    accountManager: GodToolsAccountManager,
    private val syncService: GodToolsSyncService
) : ViewModel() {
    val userInfo = accountManager.accountInfoFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val pages = MutableStateFlow(listOf(AccountPage.GLOBAL_ACTIVITY))

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            syncsRunning.value++
            syncService.syncGlobalActivity(force)
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
