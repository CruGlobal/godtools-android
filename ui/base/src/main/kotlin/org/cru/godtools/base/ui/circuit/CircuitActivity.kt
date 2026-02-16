package org.cru.godtools.base.ui.circuit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getParcelableExtraCompat
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

private const val EXTRA_SCREEN = "screen"

fun Context.createCircuitActivityIntent(screen: Screen) = Intent(this, CircuitActivity::class.java)
    .putExtra(EXTRA_SCREEN, screen as Parcelable)

@AndroidEntryPoint
class CircuitActivity : BaseActivity() {
    @Inject
    internal lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val screen = intent.getParcelableExtraCompat(EXTRA_SCREEN, Screen::class.java) ?: return finish()

        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    val backStack = rememberSaveableBackStack(screen)
                    val navigator = rememberAndroidScreenAwareNavigator(rememberCircuitNavigator(backStack), this)
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                        decoratorFactory = remember(navigator) {
                            GestureNavigationDecorationFactory(
                                onBackInvoked = navigator::pop
                            )
                        }
                    )
                }
            }
        }
    }
}
