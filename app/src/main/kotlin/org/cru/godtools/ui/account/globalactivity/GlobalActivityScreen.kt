package org.cru.godtools.ui.account.globalactivity

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.GlobalActivityAnalytics

@Parcelize
data object GlobalActivityScreen : Screen {
    data class UiState(val activity: GlobalActivityAnalytics)
}
