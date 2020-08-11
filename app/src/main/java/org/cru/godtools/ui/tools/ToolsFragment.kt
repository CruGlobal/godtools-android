package org.cru.godtools.ui.tools

import android.app.Dialog
import android.graphics.drawable.NinePatchDrawable
import android.os.AsyncTask
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.sergivonavi.materialbanner.BannerInterface
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.ccci.gto.android.common.recyclerview.advrecyclerview.draggable.SimpleOnItemDragEventListener
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.ccci.gto.android.common.util.findListener
import org.cru.godtools.R
import org.cru.godtools.adapter.BannerHeaderAdapter
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.databinding.ToolsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.tutorial.analytics.model.ADOBE_TUTORIAL_HOME_DISMISS
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.ui.tools.analytics.model.ToolOpenTapAnalyticsActionEvent
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.GodToolsDao
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import java.util.Locale
import javax.inject.Inject

class ToolsFragment() : BasePlatformFragment<ToolsFragmentBinding>(R.layout.tools_fragment), ToolsAdapterCallbacks {
    companion object {
        const val MODE_ADDED = 1
        const val MODE_AVAILABLE = 2
        const val MODE_ALL = 3
    }

    interface Callbacks {
        fun onToolInfo(code: String?)
        fun onToolSelect(code: String?, type: Tool.Type, vararg languages: Locale?)
        fun onNoToolsAvailableAction()
    }

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    private var mode: Int by argOrDefault(MODE_ADDED)

    @Inject
    internal lateinit var dao: GodToolsDao
    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.fragment = this
        binding.setTools(dataModel.tools)
        setupToolsList(binding)
    }

    @CallSuper
    public override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }

    override fun onToolInfo(code: String?) {
        findListener<Callbacks>()?.onToolInfo(code)
    }

    override fun onToolsReordered(vararg ids: Long) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            dao.updateToolOrder(*ids)
            eventBus.post(ToolUpdateEvent)
        }
    }

    fun onEmptyActionClick() {
        findListener<Callbacks>()?.onNoToolsAvailableAction()
    }

    override fun onPause() {
        toolsDragDropManager?.cancelDrag()
        super.onPause()
    }

    override fun onDestroyBinding(binding: ToolsFragmentBinding) {
        cleanupToolsList(binding)
        super.onDestroyBinding(binding)
    }
    // endregion Lifecycle

    // region Banners
    private fun createBannerWrappedAdapter(adapter: RecyclerView.Adapter<*>, lifecycleOwner: LifecycleOwner) =
        BannerHeaderAdapter().apply {
            setAdapter(adapter)
            dataModel.banner.observe(lifecycleOwner) { banner = it }
            primaryCallback = BannerInterface.OnClickListener { bannerPrimaryCallback() }
            secondaryCallback = BannerInterface.OnClickListener { bannerSecondaryCallback() }
        }

    private fun bannerPrimaryCallback() {
        when (dataModel.banner.value) {
            BannerType.TUTORIAL_TRAINING -> openTrainingTutorial()
            BannerType.TOOL_LIST_FAVORITES -> settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
        }
    }

    private fun bannerSecondaryCallback() {
        when (dataModel.banner.value) {
            BannerType.TUTORIAL_TRAINING -> {
                eventBus.post(TutorialAnalyticsActionEvent(ADOBE_TUTORIAL_HOME_DISMISS))
                settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING)
            }
        }
    }

    private fun openTrainingTutorial() {
        activity?.startTutorialActivity(PageSet.TRAINING)
    }
    // endregion Banners

    // region Data Model
    private val dataModel: ToolsFragmentDataModel by viewModels()
    private fun setupDataModel() {
        dataModel.mode.value = mode
    }
    // endregion Data Model

    // region ToolsAdapterCallbacks
    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        if (tool != null) {
            findListener<Callbacks>()?.onToolSelect(tool.code, tool.type, primary?.languageCode, parallel?.languageCode)
            eventBus.post(ToolOpenTapAnalyticsActionEvent)
        }
    }

    override fun addTool(code: String?) {
        code?.let { downloadManager.addTool(it) }
    }

    override fun removeTool(tool: Tool?, translation: Translation?) {
        when (mode) {
            MODE_ADDED -> showRemoveFavoriteConfirmationDialog(tool, translation)
            else -> removeFavorite(tool?.code)
        }
    }
    // endregion ToolsAdapterCallbacks

    // region Remove Favorite
    private fun showRemoveFavoriteConfirmationDialog(tool: Tool?, translation: Translation?) {
        RemoveFavoriteConfirmationDialogFragment(tool?.code ?: return, translation.getName(tool, null).toString())
            .show(childFragmentManager, null)
    }

    internal fun removeFavorite(code: String?) {
        code?.let { downloadManager.removeTool(it) }
    }
    // endregion Remove Favorite

    // region Tools List
    private val toolsAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, ViewModelProvider(this)).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.tools.observe(this, adapter)
        }
    }

    private var toolsDragDropManager: RecyclerViewDragDropManager? = null
    private var toolsDragDropAdapter: RecyclerView.Adapter<*>? = null

    private fun setupToolsList(binding: ToolsFragmentBinding) {
        binding.tools.setHasFixedSize(false)

        // create base tools adapter
        var adapter: RecyclerView.Adapter<*> = toolsAdapter

        // configure the DragDrop RecyclerView components (Only for Added tools)
        if (mode == MODE_ADDED) {
            toolsDragDropManager = RecyclerViewDragDropManager().apply {
                setInitiateOnMove(false)
                setInitiateOnLongPress(true)
                setDraggingItemShadowDrawable(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.material_shadow_z3) as? NinePatchDrawable
                )

                toolsDragDropAdapter = createWrappedAdapter(adapter)
                    .also { adapter = it }

                // HACK: Because the RecyclerView isn't the direct child of the SwipeRefreshLayout,
                //       reordering items in the RecyclerView doesn't cooperate correctly with the SwipeRefreshLayout.
                //       We work around this problem by disabling the SwipeRefreshLayout when we are dragging an item.
                onItemDragEventListener = object : SimpleOnItemDragEventListener() {
                    override fun onItemDragStarted(position: Int) {
                        binding.refresh.isEnabled = binding.refresh.isRefreshing
                    }

                    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
                        binding.refresh.isEnabled = true
                    }
                }
            }
            binding.tools.itemAnimator = DraggableItemAnimator()
        }

        // add banner header adapter
        adapter = createBannerWrappedAdapter(adapter, viewLifecycleOwner)

        // attach the correct adapter to the tools RecyclerView
        binding.tools.adapter = adapter

        // handle some post-adapter configuration
        toolsDragDropManager?.attachRecyclerView(binding.tools)
    }

    private fun cleanupToolsList(binding: ToolsFragmentBinding) {
        binding.tools.itemAnimator = null
        binding.tools.adapter = null

        toolsDragDropManager?.release()
        toolsDragDropManager = null
        WrapperAdapterUtils.releaseAll(toolsDragDropAdapter)
        toolsDragDropAdapter = null
    }
    // endregion Tools List
}

class RemoveFavoriteConfirmationDialogFragment() : DialogFragment() {
    constructor(code: String, name: String) : this() {
        this.code = code
        this.name = name
    }

    private var code: String by arg()
    private var name: String by arg()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.tools_list_remove_favorite_dialog_title, name))
            .setPositiveButton(R.string.tools_list_remove_favorite_dialog_confirm) { _, _ ->
                findListener<ToolsFragment>()?.removeFavorite(code)
            }
            .setNegativeButton(R.string.tools_list_remove_favorite_dialog_dismiss, null)
            .create()
    }
}
