package org.cru.godtools.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

// temporary Circuit Navigator to use as we migrate various UIs to Circuit
@Composable
fun rememberInterceptingNavigator(
    delegate: Navigator,
    goTo: (screen: Screen, delegate: Navigator) -> Unit = { screen, delegate -> delegate.goTo(screen) },
): Navigator {
    val goTo by rememberUpdatedState(goTo)

    return remember(delegate) {
        object : Navigator by delegate {
            override fun goTo(screen: Screen) = goTo(screen, delegate)
        }
    }
}
