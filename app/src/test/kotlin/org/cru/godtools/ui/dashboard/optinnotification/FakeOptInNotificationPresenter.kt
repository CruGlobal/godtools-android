package org.cru.godtools.ui.dashboard.optinnotification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.slack.circuit.runtime.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiState

class FakeOptInNotificationPresenter : OptInNotificationPresenter {
    val uiState = MutableStateFlow(UiState())

    @Composable
    override fun present(navigator: Navigator): UiState = uiState.collectAsState().value
}
