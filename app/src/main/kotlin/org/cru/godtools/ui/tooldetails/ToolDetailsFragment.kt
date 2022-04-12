package org.cru.godtools.ui.tooldetails

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.viewpager2.widget.setHeightWrapContent
import org.ccci.gto.android.common.material.tabs.notifyChanged
import org.cru.godtools.R
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.databinding.ToolDetailsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.ui.tools.analytics.model.AboutToolButtonAnalyticsActionEvent
import org.cru.godtools.util.openToolActivity
import splitties.fragmentargs.arg

@AndroidEntryPoint
class ToolDetailsFragment() :
    BasePlatformFragment<ToolDetailsFragmentBinding>(R.layout.tool_details_fragment), LinkClickedListener {
    constructor(toolCode: String) : this() {
        this.toolCode = toolCode
    }

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    internal lateinit var manifestManager: Lazy<ManifestManager>
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    private var toolCode: String by arg()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        downloadLatestTranslation()
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolDetailsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.fragment = this
        binding.tool = dataModel.tool
        binding.manifest = dataModel.primaryManifest
        binding.banner = dataModel.banner
        binding.bannerAnimation = dataModel.bannerAnimation
        binding.primaryTranslation = dataModel.primaryTranslation
        binding.parallelTranslation = dataModel.parallelTranslation
        binding.setDownloadProgress(dataModel.downloadProgress)

        binding.setupPages()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
    }

    override fun onLinkClicked(url: String) {
        eventBus.post(ExitLinkActionEvent(toolCode, Uri.parse(url)))
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            dataModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    // region Data Model
    private val dataModel: ToolDetailsFragmentDataModel by viewModels()

    private fun setupDataModel() {
        dataModel.toolCode.value = toolCode
    }
    // endregion Data Model

    // region Data Binding
    fun addTool(toolCode: String?) {
        if (toolCode != null) downloadManager.pinToolAsync(toolCode)
    }

    fun removeTool(toolCode: String?) {
        if (toolCode != null) downloadManager.unpinToolAsync(toolCode)
    }

    fun openTool(tool: Tool?, primaryTranslation: Translation?, parallelTranslation: Translation?) {
        tool?.code?.let { code ->
            val primaryLanguage = primaryTranslation?.languageCode ?: Locale.ENGLISH
            val parallelLanguage = parallelTranslation?.languageCode

            // start pre-loading the tool in the primary language
            manifestManager.get().preloadLatestPublishedManifest(code, primaryLanguage)
            if (parallelLanguage != null) {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage, parallelLanguage)
            } else {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage)
            }
        }
        eventBus.post(AboutToolButtonAnalyticsActionEvent)
    }

    fun openToolTraining(tool: Tool?, translation: Translation?) =
        launchTrainingTips(tool?.code, tool?.type, translation?.languageCode)
    // endregion Data Binding

    // region Pin Shortcut
    private fun Menu.setupPinShortcutAction() {
        findItem(R.id.action_pin_shortcut)?.let { item ->
            dataModel.shortcut.observe(this@ToolDetailsFragment, item) { isVisible = it != null }
        }
    }
    // endregion Pin Shortcut

    // region Pages
    private fun ToolDetailsFragmentBinding.setupPages() {
        // Setup the ViewPager
        pages.setHeightWrapContent()
        pages.offscreenPageLimit = 2
        pages.adapter = ToolDetailsPagerAdapter(viewLifecycleOwner, dataModel, this@ToolDetailsFragment)

        // Setup the TabLayout
        val mediator = TabLayoutMediator(tabs, pages) { tab: TabLayout.Tab, i: Int ->
            when (i) {
                0 -> tab.setText(R.string.label_tools_about)
                1 -> {
                    val count = dataModel.availableLanguages.value?.size ?: 0
                    tab.text = resources.getQuantityString(R.plurals.label_tools_languages, count, count)
                }
            }
        }
        mediator.attach()
        dataModel.availableLanguages.observe(viewLifecycleOwner) { mediator.notifyChanged() }
    }
    // endregion Pages

    // region Training Tips
    private val selectedTool by viewModels<SelectedToolSavedState>()
    private val tipsTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        if (it == RESULT_OK) launchTrainingTips(skipTutorial = true)
    }

    private fun downloadLatestTranslation() {
        downloadManager.downloadLatestPublishedTranslationAsync(toolCode, settings.primaryLanguage)
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
