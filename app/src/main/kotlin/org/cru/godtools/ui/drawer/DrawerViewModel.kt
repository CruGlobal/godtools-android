package org.cru.godtools.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.account.GodToolsAccountManager

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val accountManager: GodToolsAccountManager,
) : ViewModel() {
    val isAuthenticatedFlow = accountManager.isAuthenticatedFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // region Actions
    fun logout() = viewModelScope.launch { withContext(NonCancellable) { accountManager.logout() } }
    // endregion Actions
}
