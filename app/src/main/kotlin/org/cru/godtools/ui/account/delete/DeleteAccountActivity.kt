package org.cru.godtools.ui.account.delete

import android.content.Context
import android.content.Intent
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

fun Context.startDeleteAccountActivity() = startActivity(Intent(this, DeleteAccountActivity::class.java))

@AndroidEntryPoint
class DeleteAccountActivity : BaseActivity() {
    @Inject
    lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    val backStack = rememberSaveableBackStack(DeleteAccountScreen)
                    val navigator = rememberCircuitNavigator(backStack)
                    NavigableCircuitContent(navigator, backStack)
                }
            }
        }
    }
}
