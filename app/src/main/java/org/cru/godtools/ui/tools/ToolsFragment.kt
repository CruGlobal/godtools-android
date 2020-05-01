package org.cru.godtools.ui.tools

import android.graphics.drawable.NinePatchDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.sergivonavi.materialbanner.BannerInterface
import org.ccci.gto.android.common.recyclerview.advrecyclerview.draggable.SimpleOnItemDragEventListener
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.ccci.gto.android.common.util.findListener
import org.cru.godtools.R
import org.cru.godtools.adapter.BannerHeaderAdapter
import org.cru.godtools.base.Settings
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.ToolsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.tutorial.analytics.model.ADOBE_TUTORIAL_HOME_DISMISS
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.GodToolsDao
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
        binding.setTools(dataModel.tools)
        inflateEmptyListUi(binding)
        setupToolsList(binding)
    }

    override fun onStart() {
        super.onStart()
        updateVisibleBanner()
    }

    @CallSuper
    public override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }

    override fun onUpdateFeatureDiscovery(feature: String) {
        super.onUpdateFeatureDiscovery(feature)
        if (Settings.FEATURE_TUTORIAL_TRAINING == feature) updateVisibleBanner()
    }

    override fun onToolSelect(code: String?, type: Tool.Type, vararg languages: Locale?) {
        findListener<Callbacks>()?.onToolSelect(code, type, *languages)
    }

    override fun onToolInfo(code: String?) {
        findListener<Callbacks>()?.onToolInfo(code)
    }

    override fun onToolAdd(code: String?) {
        code?.let { downloadManager.addTool(it) }
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
    private fun updateVisibleBanner() {
        if (toolsHeaderAdapter != null) {
            if (!settings.isFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING) && mode == MODE_ADDED &&
                PageSet.TRAINING.supportsLocale(requireContext().deviceLocale)
            ) {
                toolsHeaderAdapter!!.banner = BannerType.TUTORIAL_TRAINING
                toolsHeaderAdapter!!.primaryCallback =
                    BannerInterface.OnClickListener { openTrainingTutorial() }
                toolsHeaderAdapter!!.secondaryCallback = BannerInterface.OnClickListener {
                    eventBus.post(TutorialAnalyticsActionEvent(ADOBE_TUTORIAL_HOME_DISMISS))
                    settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING)
                }
            } else {
                toolsHeaderAdapter!!.banner = null
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

    // region Tools List
    private val toolsAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, ViewModelProvider(this)).also {
            it.callbacks.set(this)
            dataModel.tools.observe(this, it)
        }
    }

    private var toolsHeaderAdapter: BannerHeaderAdapter? = null

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

        // configure banner view if required for the current mode
        if (mode == MODE_ADDED) {
            toolsHeaderAdapter = BannerHeaderAdapter().apply {
                setAdapter(adapter)
                adapter = this
            }
        }

        // attach the correct adapter to the tools RecyclerView
        binding.tools.adapter = adapter

        // handle some post-adapter configuration
        toolsDragDropManager?.attachRecyclerView(binding.tools)

        updateVisibleBanner()
    }

    private fun cleanupToolsList(binding: ToolsFragmentBinding) {
        binding.tools.itemAnimator = null
        binding.tools.adapter = null

        toolsDragDropManager?.release()
        toolsDragDropManager = null
        WrapperAdapterUtils.releaseAll(toolsDragDropAdapter)
        toolsDragDropAdapter = null

        toolsHeaderAdapter = null
    }
    // endregion Tools List

    // region Empty List UI
    private fun inflateEmptyListUi(binding: ToolsFragmentBinding) {
        val layout = when (mode) {
            MODE_ADDED -> R.layout.tools_added_empty_ui
            else -> R.layout.tools_available_empty_ui
        }
        val emptyUi = LayoutInflater.from(binding.emptyListUi.context).inflate(layout, binding.emptyListUi)

        // HACK: quick and dirty way to attach an OnClickListener
        emptyUi.findViewById<View>(R.id.action)?.setOnClickListener { _ -> onEmptyActionClick() }
    }
    // endregion Empty List UI
}
