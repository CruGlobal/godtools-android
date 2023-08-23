package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_TOOL_DETAILS
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.ui.tools.EXTRA_ADDITIONAL_LANGUAGE
import org.cru.godtools.util.openToolActivity

fun Activity.startToolDetailsActivity(toolCode: String, additionalLanguage: Locale? = null) = startActivity(
    Intent(this, ToolDetailsActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_TOOL, toolCode)
        .putExtra(EXTRA_ADDITIONAL_LANGUAGE, additionalLanguage)
)

@AndroidEntryPoint
class ToolDetailsActivity : BaseActivity() {
    private val viewModel: ToolDetailsViewModel by viewModels()

    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // finish now if we don't have a valid start state
        if (!isValidStartState) {
            finish()
            return
        }

        setContent {
            GodToolsTheme {
                ToolDetailsLayout(
                    viewModel = viewModel,
                    onEvent = {
                        when (it) {
                            is ToolDetailsEvent.NavigateUp -> onNavigateUp()
                            is ToolDetailsEvent.OpenTool -> openTool(it.tool, it.lang1, it.lang2)
                            is ToolDetailsEvent.OpenToolTraining -> {
                                launchTrainingTips(it.tool?.code, it.tool?.type, it.lang)
                            }
                            is ToolDetailsEvent.PinShortcut -> shortcutManager.pinShortcut(it.shortcut)
                        }
                    }
                )
            }
        }
    }
    // endregion Lifecycle

    private val isValidStartState get() = viewModel.toolCode.value != null

    private fun openTool(tool: Tool?, lang1: Locale?, lang2: Locale?) {
        tool?.code?.let { code ->
            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, code, SOURCE_TOOL_DETAILS))
            val languages = listOfNotNull(lang1 ?: Locale.ENGLISH, lang2)
            openToolActivity(code, tool.type, *languages.toTypedArray())
        }
    }

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
