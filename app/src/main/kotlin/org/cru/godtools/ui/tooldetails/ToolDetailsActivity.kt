package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_TOOL_DETAILS
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.tool.BaseToolRendererModule
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ToolDetailsActivityBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.util.openToolActivity

fun Activity.startToolDetailsActivity(toolCode: String) = startActivity(
    Intent(this, ToolDetailsActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_TOOL, toolCode)
)

@AndroidEntryPoint
class ToolDetailsActivity : BasePlatformActivity<ToolDetailsActivityBinding>() {
    private val viewModel: ToolDetailsViewModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // finish now if we don't have a valid start state
        if (!isValidStartState) {
            finish()
            return
        }

        downloadLatestTranslation()
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        title = ""
    }

    override fun onBindingChanged() {
        binding.compose.setContent {
            GodToolsTheme {
                ToolDetailsLayout(
                    viewModel = viewModel,
                    onEvent = {
                        when (it) {
                            is ToolDetailsEvent.OpenTool -> openTool(it.tool, it.lang1, it.lang2)
                            is ToolDetailsEvent.OpenToolTraining -> {
                                launchTrainingTips(it.tool?.code, it.tool?.type, it.lang)
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            viewModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    private val isValidStartState get() = viewModel.toolCode.value != null

    // region UI
    override val toolbar get() = binding.appbar
    override val drawerLayout get() = binding.drawerLayout
    override val drawerMenu get() = binding.drawerMenu

    override fun inflateBinding() = ToolDetailsActivityBinding.inflate(layoutInflater)
    // endregion UI

    private fun openTool(tool: Tool?, lang1: Locale?, lang2: Locale?) {
        tool?.code?.let { code ->
            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, code, SOURCE_TOOL_DETAILS))
            val languages = listOfNotNull(lang1 ?: Locale.ENGLISH, lang2)
            openToolActivity(code, tool.type, *languages.toTypedArray())
        }
    }

    // region Pin Shortcut
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    private fun Menu.setupPinShortcutAction() {
        findItem(R.id.action_pin_shortcut)?.let { item ->
            viewModel.shortcut.observe(this@ToolDetailsActivity, item) { isVisible = it != null }
        }
    }
    // endregion Pin Shortcut

    // region Training Tips
    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    @Named(BaseToolRendererModule.IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>

    private val selectedTool by viewModels<SelectedToolSavedState>()
    private val tipsTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        if (it == RESULT_OK) launchTrainingTips(skipTutorial = true)
    }

    private fun downloadLatestTranslation() {
        observe(viewModel.toolCode.asLiveData(), settings.primaryLanguageLiveData, isConnected) { t, l, _ ->
            if (t != null) downloadManager.downloadLatestPublishedTranslationAsync(t, l)
        }
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
