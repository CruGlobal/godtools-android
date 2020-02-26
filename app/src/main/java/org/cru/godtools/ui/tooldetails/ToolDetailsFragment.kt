package org.cru.godtools.ui.tooldetails

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.google.android.material.tabs.notifyPagerAdapterChanged
import org.ccci.gto.android.common.util.findListener
import org.cru.godtools.R
import org.cru.godtools.base.Constants
import org.cru.godtools.databinding.ToolDetailsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BaseBindingPlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.shortcuts.GodToolsShortcutManager.PendingShortcut
import org.cru.godtools.util.openToolActivity
import java.util.Locale

class ToolDetailsFragment : BaseBindingPlatformFragment<ToolDetailsFragmentBinding>(R.layout.tool_details_fragment) {
    interface Callbacks {
        fun onToolAdded()
        fun onToolRemoved()
    }

    private val downloadManager by lazy { GodToolsDownloadManager.getInstance(requireContext()) }
    private val shortcutManager by lazy { GodToolsShortcutManager.getInstance(requireContext()) }

    // these properties should be treated as final and only set/modified in onCreate()
    var mToolCode = Tool.INVALID_CODE

    private var mLatestParallelTranslation: Translation? = null

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val args = arguments
        if (args != null) {
            mToolCode = args.getString(Constants.EXTRA_TOOL, mToolCode)
        }
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolDetailsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.fragment = this
        binding.tool = dataModel.tool
        binding.setBanner(dataModel.banner)
        binding.primaryTranslation = dataModel.primaryTranslation
        binding.setDownloadProgress(dataModel.downloadProgress)

        binding.setupOverviewVideo()
        binding.setupViewPager()
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

    private fun onLoadLatestParallelTranslation(translation: Translation?) {
        mLatestParallelTranslation = translation
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        cleanupPinShortcutAction()
    }
    // endregion Lifecycle

    // region Data Model
    private val dataModel: ToolDetailsFragmentDataModel by viewModels()

    private fun setupDataModel() {
        dataModel.toolCode.value = mToolCode
        dataModel.parallelTranslation.observe(this) { onLoadLatestParallelTranslation(it) }
    }
    // endregion Data Model

    // region Data Binding
    fun addTool(toolCode: String?) {
        if (toolCode != null) {
            downloadManager.addTool(toolCode)
            findListener<Callbacks>()?.onToolAdded()
        }
    }

    fun removeTool(toolCode: String?) {
        if (toolCode != null) {
            downloadManager.removeTool(toolCode)
            findListener<Callbacks>()?.onToolRemoved()
        }
    }

    fun openTool(tool: Tool?, primaryTranslation: Translation?) {
        tool?.code?.let { code ->
            val primaryLanguage = primaryTranslation?.languageCode ?: Locale.ENGLISH
            val parallelLanguage = mLatestParallelTranslation?.languageCode
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
            Observer<PendingShortcut?> { action.isVisible = it != null }
                .also { dataModel.shortcut.observe(this@ToolDetailsFragment, it) }
        }
    }

    private fun cleanupPinShortcutAction() {
        pinShortcutObserver?.let { dataModel.shortcut.removeObserver(it) }
        pinShortcutObserver = null
    }
    // endregion Pin Shortcut

    // region Overview Video
    private fun ToolDetailsFragmentBinding.setupOverviewVideo() = viewLifecycleOwner.lifecycle.addObserver(videoBanner)
    // endregion Overview Video

    // region ViewPager
    private fun ToolDetailsFragmentBinding.setupViewPager() {
        detailViewPager.adapter = ToolDetailsPagerAdapter(requireContext(), viewLifecycleOwner, dataModel)
        detailTabLayout.setupWithViewPager(detailViewPager, true)
        dataModel.availableLanguages.observe(viewLifecycleOwner) { detailTabLayout.notifyPagerAdapterChanged() }
    }
    // endregion ViewPager

    companion object {
        fun newInstance(code: String?): Fragment {
            val fragment = ToolDetailsFragment()
            val args = Bundle(1)
            args.putString(Constants.EXTRA_TOOL, code)
            fragment.arguments = args
            return fragment
        }
    }
}
