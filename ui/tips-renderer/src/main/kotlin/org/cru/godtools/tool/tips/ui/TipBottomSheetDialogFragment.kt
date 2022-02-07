package org.cru.godtools.tool.tips.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.androidx.viewpager2.widget.currentItemLiveData
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.material.bottomsheet.BindingBottomSheetDialogFragment
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tool.tips.R
import org.cru.godtools.tool.tips.analytics.model.TipAnalyticsScreenEvent
import org.cru.godtools.tool.tips.databinding.ToolTipBinding
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TrainingTipTable
import org.keynote.godtools.android.db.GodToolsDao
import splitties.fragmentargs.arg

@AndroidEntryPoint
class TipBottomSheetDialogFragment : BindingBottomSheetDialogFragment<ToolTipBinding>(R.layout.tool_tip), TipCallbacks {
    companion object {
        fun create(tip: Tip): TipBottomSheetDialogFragment? = TipBottomSheetDialogFragment().apply {
            tool = tip.manifest.code ?: return null
            locale = tip.manifest.locale ?: return null
            this.tip = tip.id
        }
    }

    interface Callbacks {
        fun onDismissTip()
    }

    private var tool: String by arg()
    private var locale: Locale by arg()
    private var tip: String by arg()

    @Inject
    internal lateinit var dao: GodToolsDao
    @Inject
    internal lateinit var eventBus: EventBus

    private val dataModel: TipBottomSheetDialogFragmentDataModel by viewModels()
    private val toolState: ToolStateHolder by activityViewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.toolCode.value = tool
        dataModel.locale.value = locale
        dataModel.tipId.value = tip
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forceExpandedMode()
    }

    override fun onStart() {
        super.onStart()
        if (context?.resources?.getBoolean(R.bool.tract_tips_show_full_height) == true) makeFullScreen()
    }

    override fun onBindingCreated(binding: ToolTipBinding, savedInstanceState: Bundle?) {
        binding.callbacks = this
        binding.tip = dataModel.tip
        binding.isComplete = dataModel.isCompleted
        binding.setupPages()
    }

    override fun onDismiss(dialog: DialogInterface) {
        findListener<Callbacks>()?.onDismissTip()
        super.onDismiss(dialog)
    }

    override fun onDestroyBinding(binding: ToolTipBinding) {
        super.onDestroyBinding(binding)
        cleanupPages()
    }
    // endregion Lifecycle

    // region Pages
    private var pages: ViewPager2? = null

    @Inject
    internal lateinit var tipPageAdapterFactory: TipPageAdapter.Factory

    private fun ToolTipBinding.setupPages() {
        this@TipBottomSheetDialogFragment.pages = pages
        pages.adapter = tipPageAdapterFactory.create(viewLifecycleOwner, toolState.toolState).also {
            dataModel.tip.observe(viewLifecycleOwner, it)
            it.callbacks = callbacks
        }
        pages.currentItemLiveData.observe(viewLifecycleOwner) { trackScreenAnalytics(it) }
    }

    private fun cleanupPages() {
        this@TipBottomSheetDialogFragment.pages = null
    }

    // region TipPageController.Callbacks
    override fun goToNextPage() {
        pages?.apply { currentItem += 1 }
    }

    override fun closeTip(completed: Boolean) {
        if (completed) {
            val trainingTip = TrainingTip(tool, locale, tip)
            trainingTip.isCompleted = true
            @Suppress("DeferredResultUnused")
            dao.updateOrInsertAsync(trainingTip, TrainingTipTable.COLUMN_IS_COMPLETED)
        }
        dismissAllowingStateLoss()
    }
    // endregion TipPageController.Callbacks
    // endregion Pages

    private fun trackScreenAnalytics(page: Int) {
        eventBus.post(TipAnalyticsScreenEvent(tool, locale, tip, page))
    }

    private fun forceExpandedMode() {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun makeFullScreen() {
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.apply {
            layoutParams = layoutParams.apply { height = ViewGroup.LayoutParams.MATCH_PARENT }
        }
    }
}

@HiltViewModel
internal class TipBottomSheetDialogFragmentDataModel @Inject constructor(
    dao: GodToolsDao,
    manifestManager: ManifestManager
) : LatestPublishedManifestDataModel(manifestManager) {
    val tipId = MutableLiveData<String>()

    val tip = manifest.combineWith(tipId.distinctUntilChanged()) { m, t -> m?.findTip(t) }
    val isCompleted = toolCode.switchCombineWith(locale, tipId) { tool, locale, tipId ->
        when {
            tool == null || locale == null || tipId == null -> emptyLiveData()
            else -> dao.findLiveData<TrainingTip>(tool, locale, tipId)
        }
    }.map { it?.isCompleted == true }
}
