package org.cru.godtools.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okta.authfoundationbootstrap.CredentialBootstrap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.okta.authfoundation.credential.idTokenFlow
import org.ccci.gto.android.common.okta.authfoundationbootstrap.defaultCredentialFlow
import org.cru.godtools.sync.GodToolsSyncService

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel @Inject internal constructor(
    credentials: CredentialBootstrap,
    private val syncService: GodToolsSyncService
) : ViewModel() {
    val idToken = credentials.defaultCredentialFlow()
        .flatMapLatest { it.idTokenFlow() }
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
