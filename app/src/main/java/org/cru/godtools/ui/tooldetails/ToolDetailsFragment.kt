package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
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
import org.cru.godtools.shortcuts.PendingShortcut
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.buildTutorialActivityIntent
import org.cru.godtools.ui.tools.analytics.model.AboutToolButtonAnalyticsActionEvent
import org.cru.godtools.util.openToolActivity
import splitties.fragmentargs.arg

private const val REQUEST_TUTORIAL_TIPS = 102

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
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolDetailsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.fragment = this
        binding.tool = dataModel.tool
        binding.manifest = dataModel.primaryManifest
        binding.setBanner(dataModel.banner)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_TUTORIAL_TIPS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    settings.setFeatureDiscovered("$FEATURE_TUTORIAL_TIPS${dataModel.tipsTool}")
                    val code = dataModel.tipsTool ?: return
                    val type = dataModel.tipsType ?: return
                    val language = dataModel.tipsLanguage ?: return
                    launchTrainingTips(code, type, language, true)
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLinkClicked(url: String) {
        eventBus.post(ExitLinkActionEvent(Uri.parse(url)))
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            dataModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        cleanupPinShortcutAction()
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

    fun openToolTraining(tool: Tool?, translation: Translation?) {
        val code = tool?.code ?: return
        val locale = translation?.languageCode ?: return

        dataModel.tipsLanguage = locale
        dataModel.tipsTool = code
        dataModel.tipsType = tool.type
        launchTrainingTips(code, tool.type, locale, false)
    }

    private fun launchTrainingTips(
        code: String,
        type: Tool.Type,
        locale: Locale,
        force: Boolean
    ) {
        if (force || settings.isFeatureDiscovered("$FEATURE_TUTORIAL_TIPS$code"))
            requireActivity().openToolActivity(code, type, locale, showTips = true)
        else startActivityForResult(context?.buildTutorialActivityIntent(PageSet.TIPS), REQUEST_TUTORIAL_TIPS)
    }
    // endregion Data Binding

    // region Pin Shortcut
    private var pinShortcutObserver: Observer<PendingShortcut?>? = null

    private fun Menu.setupPinShortcutAction() {
        pinShortcutObserver = findItem(R.id.action_pin_shortcut)
            ?.let { action -> Observer<PendingShortcut?> { action.isVisible = it != null } }
            ?.also { dataModel.shortcut.observe(this@ToolDetailsFragment, it) }
    }

    private fun cleanupPinShortcutAction() {
        pinShortcutObserver?.let { dataModel.shortcut.removeObserver(it) }
        pinShortcutObserver = null
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
}
