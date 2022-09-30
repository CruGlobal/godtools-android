package org.cru.godtools.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.okta.authfoundationbootstrap.CredentialBootstrap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.okta.authfoundation.credential.idTokenFlow
import org.ccci.gto.android.common.okta.authfoundationbootstrap.defaultCredentialFlow

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel @Inject internal constructor(
    credentials: CredentialBootstrap
) : ViewModel() {
    val idToken = credentials.defaultCredentialFlow()
        .flatMapLatest { it.idTokenFlow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val pages = MutableStateFlow(listOf(AccountPage.GLOBAL_ACTIVITY))
}
