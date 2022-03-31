package org.cru.godtools.ui.tools

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.sergivonavi.materialbanner.BannerInterface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.ccci.gto.android.common.recyclerview.advrecyclerview.draggable.SimpleOnItemDragEventListener
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.adapter.BannerHeaderAdapter
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_MY_TOOLS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ALL_TOOLS
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_HOME
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_LESSONS
import org.cru.godtools.base.Settings
import org.cru.godtools.databinding.ToolsFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.tutorial.analytics.model.TUTORIAL_HOME_DISMISS
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.GodToolsDao
import splitties.fragmentargs.argOrDefault

@AndroidEntryPoint
class ToolsListFragment() : BasePlatformFragment<ToolsFragmentBinding>(R.layout.tools_fragment), ToolsAdapterCallbacks {
    companion object {
        const val MODE_ADDED = 1
        const val MODE_ALL = 2
        const val MODE_LESSONS = 3
    }

    interface Callbacks : ToolsAdapterCallbacks {
        fun onNoToolsAvailableAction()
    }

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    private var mode by argOrDefault(MODE_ADDED)

    @Inject
    internal lateinit var dao: GodToolsDao

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.fragment = this
        binding.refresh.setupSwipeRefresh()
        binding.setTools(dataModel.tools)
        setupToolsList(binding)
    }

    override fun onResume() {
        super.onResume()
        trackInAnalytics()
    }

    @CallSuper
    public override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }

    override fun showToolDetails(code: String?) {
        findListener<ToolsAdapterCallbacks>()?.showToolDetails(code)
    }

    override fun onToolsReordered(vararg ids: Long) {
        GlobalScope.launch(Dispatchers.IO) {
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

    private fun trackInAnalytics() {
        when (mode) {
            MODE_ALL -> eventBus.post(AnalyticsScreenEvent(SCREEN_ALL_TOOLS))
            MODE_ADDED -> {
                eventBus.post(AnalyticsScreenEvent(SCREEN_HOME))
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_MY_TOOLS))
            }
            MODE_LESSONS -> eventBus.post(AnalyticsScreenEvent(SCREEN_LESSONS))
        }
    }

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
                eventBus.post(TutorialAnalyticsActionEvent(TUTORIAL_HOME_DISMISS))
                settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING)
            }
        }
    }

    private fun openTrainingTutorial() {
        activity?.startTutorialActivity(PageSet.TRAINING)
    }
    // endregion Banners

    // region Data Model
    private val dataModel: ToolsListFragmentDataModel by viewModels()
    private fun setupDataModel() {
        dataModel.mode.value = mode
    }
    // endregion Data Model

    // region ToolsAdapterCallbacks
    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        findListener<Callbacks>()?.openTool(tool, primary, parallel)
    }

    override fun pinTool(code: String?) {
        findListener<Callbacks>()?.pinTool(code)
    }

    override fun unpinTool(tool: Tool?, translation: Translation?) {
        findListener<Callbacks>()?.unpinTool(tool, translation)
    }
    // endregion ToolsAdapterCallbacks

    // region Tools List
    private val toolsAdapterDataModel by viewModels<ToolsAdapterViewModel>()
    private val toolsAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, toolsAdapterDataModel).also { adapter ->
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
