package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.ui.tools.EXTRA_ADDITIONAL_LANGUAGE
import org.cru.godtools.util.openToolActivity
import org.cru.godtools.util.rememberInterceptingNavigator

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
                        rememberInterceptingNavigator(
                            rememberCircuitNavigator(backStack),
                            goTo = { screen, delegate ->
                                when (screen) {
                                    // TODO: move this logic into the ToolDetailsPresenter once tutorials use Circuit
                                    is OpenToolTrainingScreen -> {
                                        launchTrainingTips(screen.tool, screen.type, screen.locale)
                                        true
                                    }

                                    else -> delegate.goTo(screen)
                                }
                            }
                        ),
                        this
                    )
                    CircuitContent(screen, navigator)
                }
            }
        }
    }
    // endregion Lifecycle

    private val isValidStartState get() = initialTool != null

    // region Training Tips
    private val selectedTool by viewModels<SelectedToolSavedState>()
    private val tipsTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        if (it == RESULT_OK) launchTrainingTips(skipTutorial = true)
    }

    private fun launchTrainingTips(
        code: String? = selectedTool.tool,
        type: Tool.Type? = selectedTool.type,
        locale: Locale? = selectedTool.language,
        skipTutorial: Boolean = false,
    ): Unit = when {
        code == null || type == null || locale == null -> Unit
        skipTutorial || settings.isFeatureDiscovered("$FEATURE_TUTORIAL_TIPS$code") -> {
            settings.setFeatureDiscovered("$FEATURE_TUTORIAL_TIPS$code")
            openToolActivity(code, type, locale, showTips = true)
        }
        else -> {
            selectedTool.tool = code
            selectedTool.type = type
            selectedTool.language = locale
            tipsTutorialLauncher.launch(PageSet.TIPS)
        }
    }
    // endregion Training Tips
}
