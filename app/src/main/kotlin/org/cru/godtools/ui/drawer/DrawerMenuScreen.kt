package org.cru.godtools.ui.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.BuildConfig

@Parcelize
data object DrawerMenuScreen : Screen {
    data class State(
        val drawerState: DrawerState = DrawerState(DrawerValue.Closed),
        val isLoggedIn: Boolean = false,
        val versionName: String = BuildConfig.VERSION_NAME,
        val versionCode: Int = BuildConfig.VERSION_CODE,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object Logout : Event

        // TODO: this is a temporary event to aid in migrating the logic to Circuit
        data object DismissDrawer : Event
    }
}
