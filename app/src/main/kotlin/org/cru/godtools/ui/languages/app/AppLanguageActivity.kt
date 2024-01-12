package org.cru.godtools.ui.languages.app

import android.os.Bundle
import androidx.activity.compose.setContent
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

@AndroidEntryPoint
class AppLanguageActivity : BaseActivity() {
    @Inject
    internal lateinit var circuit: Circuit

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    val backStack = rememberSaveableBackStack { push(AppLanguageScreen) }
                    val navigator = rememberCircuitNavigator(backStack)
                    NavigableCircuitContent(navigator, backStack)
                }
            }
        }
    }
    // endregion Lifecycle
}
