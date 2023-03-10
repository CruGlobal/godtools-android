package org.cru.godtools.ui.tooldetails

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_TOOL_DETAILS
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ToolDetailsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.util.openToolActivity
import splitties.bundle.put

@AndroidEntryPoint
class ToolDetailsFragment() : BasePlatformFragment<ToolDetailsFragmentBinding>() {
    constructor(toolCode: String) : this() {
        arguments = Bundle().apply {
            put(EXTRA_TOOL, toolCode)
        }
    }

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    private val dataModel: ToolDetailsViewModel by activityViewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        downloadLatestTranslation()
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ToolDetailsFragmentBinding.inflate(inflater, container, false)

    override fun onBindingCreated(binding: ToolDetailsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.compose.setContent {
            GodToolsTheme {
                ToolDetailsLayout(
                    viewModel = dataModel,
                    onOpenTool = { tool, trans1, trans2 -> openTool(tool, trans1, trans2) },
                    onOpenToolTraining = { tool, translation ->
                        launchTrainingTips(tool?.code, tool?.type, translation?.languageCode)
                    }
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            dataModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    private fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        tool?.code?.let { code ->
            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, code, SOURCE_TOOL_DETAILS))

            val primaryLanguage = primary?.languageCode ?: Locale.ENGLISH
            val parallelLanguage = parallel?.languageCode

            // start pre-loading the tool in the primary language
            if (parallelLanguage != null) {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage, parallelLanguage)
            } else {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage)
            }
        }
    }

    // region Pin Shortcut
    private fun Menu.setupPinShortcutAction() {
        findItem(R.id.action_pin_shortcut)?.let { item ->
            dataModel.shortcut.observe(this@ToolDetailsFragment, item) { isVisible = it != null }
        }
    }
    // endregion Pin Shortcut

    // region Training Tips
    @Inject
    @Named(IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>

    private val selectedTool by viewModels<SelectedToolSavedState>()
    private val tipsTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        if (it == RESULT_OK) launchTrainingTips(skipTutorial = true)
    }

    private fun downloadLatestTranslation() {
        observe(dataModel.toolCode.asLiveData(), settings.primaryLanguageLiveData, isConnected) { t, l, _ ->
            if (t != null) downloadManager.downloadLatestPublishedTranslationAsync(t, l)
        }
    }

    private fun launchTrainingTips(
        code: String? = selectedTool.tool,
        type: Tool.Type? = selectedTool.type,
        locale: Locale? = selectedTool.language,
        skipTutorial: Boolean = false
    ): Unit = when {
        code == null || type == null || locale == null -> Unit
        skipTutorial || settings.isFeatureDiscovered("$FEATURE_TUTORIAL_TIPS$code") -> {
            settings.setFeatureDiscovered("$FEATURE_TUTORIAL_TIPS$code")
            requireActivity().openToolActivity(code, type, locale, showTips = true)
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
