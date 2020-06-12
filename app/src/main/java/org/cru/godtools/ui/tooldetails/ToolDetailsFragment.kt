package org.cru.godtools.ui.tooldetails

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.Lazy
import org.ccci.gto.android.common.androidx.viewpager2.widget.setHeightWrapContent
import org.ccci.gto.android.common.material.tabs.notifyChanged
import org.cru.godtools.R
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.databinding.ToolDetailsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.shortcuts.PendingShortcut
import org.cru.godtools.util.openToolActivity
import splitties.fragmentargs.arg
import java.util.Locale
import javax.inject.Inject

class ToolDetailsFragment() : BasePlatformFragment<ToolDetailsFragmentBinding>(R.layout.tool_details_fragment),
    LinkClickedListener {
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
        binding.setBanner(dataModel.banner)
        binding.primaryTranslation = dataModel.primaryTranslation
        binding.parallelTranslation = dataModel.parallelTranslation
        binding.setDownloadProgress(dataModel.downloadProgress)

        binding.setupOverviewVideo()
        binding.setupPages()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
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
        if (toolCode != null) downloadManager.addTool(toolCode)
    }

    fun removeTool(toolCode: String?) {
        if (toolCode != null) downloadManager.removeTool(toolCode)
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
    }
    // endregion Data Binding

    // region Pin Shortcut
    private var pinShortcutObserver: Observer<PendingShortcut?>? = null

    private fun Menu.setupPinShortcutAction() {
        pinShortcutObserver = findItem(R.id.action_pin_shortcut)?.let { action ->
            dataModel.shortcut.observe(this@ToolDetailsFragment) { action.isVisible = it != null }
        }
    }

    private fun cleanupPinShortcutAction() {
        pinShortcutObserver?.let { dataModel.shortcut.removeObserver(it) }
        pinShortcutObserver = null
    }
    // endregion Pin Shortcut

    // region Overview Video
    private fun ToolDetailsFragmentBinding.setupOverviewVideo() {
        viewLifecycleOwner.lifecycle.addObserver(videoBanner)
        videoBanner.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            private lateinit var videoId: String
            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                this.videoId = videoId
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) youTubePlayer.cueVideo(videoId, 0f)
            }
        })
    }
    // endregion Overview Video

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
    // region Pages
}
