package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

private const val EXTRA_ADDITIONAL_LANGUAGE = "additionalLanguage"

fun Activity.startToolDetailsActivity(toolCode: String, additionalLanguage: Locale? = null) = startActivity(
    Intent(this, ToolDetailsActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_TOOL, toolCode)
        .putExtra(EXTRA_ADDITIONAL_LANGUAGE, additionalLanguage)
)

@AndroidEntryPoint
class ToolDetailsActivity : BaseActivity() {
    private val initialTool get() = intent?.getStringExtra(EXTRA_TOOL)

    @Inject
    internal lateinit var circuit: Circuit

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // finish now if we don't have a valid start state
        if (!isValidStartState) {
            finish()
            return
        }

        enableEdgeToEdge()
        val screen = ToolDetailsScreen(
            initialTool = initialTool!!,
            secondLanguage = intent.getSerializableExtraCompat(EXTRA_ADDITIONAL_LANGUAGE, Locale::class.java)
        )
        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    val backStack = rememberSaveableBackStack(screen)
                    val navigator = rememberAndroidScreenAwareNavigator(
                        rememberCircuitNavigator(backStack),
                        this
                    )
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                    )
                }
            }
        }
    }
    // endregion Lifecycle

    private val isValidStartState get() = initialTool != null
}
