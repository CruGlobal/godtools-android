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
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getParcelableExtraCompat
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

private const val EXTRA_SCREEN = "screen"

fun Context.startCircuitActivity(screen: Screen) = startActivity(createCircuitActivityIntent(screen))
fun Context.createCircuitActivityIntent(screen: Screen) = Intent(this, CircuitActivity::class.java)
    .putExtra(EXTRA_SCREEN, screen as Parcelable)

@AndroidEntryPoint
class CircuitActivity : BaseActivity() {
    @Inject
    internal lateinit var circuit: Circuit

    @Inject
    internal lateinit var deepLinkParsers: Set<@JvmSuppressWildcards CircuitDeepLinkParser>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialScreens = intent.resolveInitialScreen()

        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    ContentWithOverlays {
                        val backStack = rememberSaveableBackStack(initialScreens)
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

    private fun Intent.resolveInitialScreen(): List<Screen> {
        val uri = data

        return uri?.let { deepLinkParsers.singleOrNull { it.isDeepLinkSupported(uri) } }?.parseDeepLink(uri)
            ?: getParcelableExtraCompat(EXTRA_SCREEN, Screen::class.java)?.let { listOf(it) }
            ?: TODO("Show the DashboardScreen once it uses Circuit")
    }
}
