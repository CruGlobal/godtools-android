package org.cru.godtools.ui.tooldetails

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.viewpager2.widget.setHeightWrapContent
import org.cru.godtools.R
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TIPS
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.databinding.ToolDetailsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract
import org.cru.godtools.ui.tooldetails.analytics.model.ToolDetailsScreenEvent
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel
import org.cru.godtools.ui.tools.analytics.model.AboutToolButtonAnalyticsActionEvent
import org.cru.godtools.util.openToolActivity
import splitties.bundle.put

@AndroidEntryPoint
class ToolDetailsFragment() :
    BasePlatformFragment<ToolDetailsFragmentBinding>(R.layout.tool_details_fragment),
    LinkClickedListener,
    ToolsAdapterCallbacks {
    constructor(toolCode: String) : this() {
        arguments = Bundle().apply {
            put(EXTRA_TOOL, toolCode)
        }
    }

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    internal lateinit var manifestManager: Lazy<ManifestManager>
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    private val dataModel: ToolDetailsFragmentDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        downloadLatestTranslation()
        triggerScreenAnalyticsEventWhenResumed()
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

        binding.setupScrollView()
        binding.setupPages()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
    }

    override fun onResume() {
        super.onResume()
        dataModel.toolCode.value?.let { eventBus.post(ToolDetailsScreenEvent(it)) }
    }

    override fun onLinkClicked(url: String) {
        eventBus.post(
            ExitLinkActionEvent(
                dataModel.toolCode.value,
                Uri.parse(url),
                dataModel.primaryTranslation.value?.languageCode
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            dataModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    private fun triggerScreenAnalyticsEventWhenResumed() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                dataModel.toolCode.collect { it?.let { eventBus.post(ToolDetailsScreenEvent(it)) } }
            }
        }
    }

    // region Data Binding

    // region ToolsAdapterCallbacks
    override fun onToolClicked(tool: Tool?, primary: Translation?, parallel: Translation?) {
        showToolDetails(tool?.code)
    }

    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        tool?.code?.let { code ->
            val primaryLanguage = primary?.languageCode ?: Locale.ENGLISH
            val parallelLanguage = parallel?.languageCode

            // start pre-loading the tool in the primary language
            if (parallelLanguage != null) {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage, parallelLanguage)
            } else {
                requireActivity().openToolActivity(code, tool.type, primaryLanguage)
            }
        }
        eventBus.post(AboutToolButtonAnalyticsActionEvent)
    }

    override fun showToolDetails(code: String?) {
        dataModel.toolCode.value = code
    }

    override fun pinTool(code: String?) {
        if (code != null) downloadManager.pinToolAsync(code)
    }

    fun unpinTool(toolCode: String?) {
        if (toolCode != null) downloadManager.unpinToolAsync(toolCode)
    }

    override fun unpinTool(tool: Tool?, translation: Translation?) = unpinTool(tool?.code)
    // endregion ToolsAdapterCallbacks

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

    // region ScrollView
    private fun ToolDetailsFragmentBinding.setupScrollView() {
        dataModel.toolCode.drop(1)
            .onEach { scrollView.smoothScrollTo(0, 0) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
    // endregion ScrollView

    // region Pages
    private fun ToolDetailsFragmentBinding.setupPages() {
        // Setup the ViewPager
        pages.setHeightWrapContent()
        pages.offscreenPageLimit = 2
        pages.adapter = ToolDetailsPagerAdapter(
            viewLifecycleOwner,
            dataModel,
            VariantToolsAdapter(
                viewLifecycleOwner,
                toolsDataModel,
                R.layout.tool_details_page_variants_variant,
                dataModel.toolCodeLiveData
            ).also {
                it.callbacks.set(this@ToolDetailsFragment)
                dataModel.variants.asLiveData().observe(viewLifecycleOwner, it)
            },
            this@ToolDetailsFragment
        ).also { adapter ->
            dataModel.pages
                .onEach { adapter.pages = it }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }

        // Setup the TabLayout
        TabLayoutMediator(tabs, pages) { tab, i -> tab.setText(dataModel.pages.value[i].tabLabel) }.attach()
    }
    // endregion Pages

    // region Training Tips
    @Inject
    @Named(IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>

    private val selectedTool by viewModels<SelectedToolSavedState>()
    private val tipsTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        if (it == RESULT_OK) launchTrainingTips(skipTutorial = true)
    }

    private fun downloadLatestTranslation() {
        observe(dataModel.toolCodeLiveData, settings.primaryLanguageLiveData, isConnected) { t, l, _ ->
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
