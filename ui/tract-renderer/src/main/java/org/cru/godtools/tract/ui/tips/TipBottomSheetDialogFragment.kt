package org.cru.godtools.tract.ui.tips

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.fragment.BaseBottomSheetDialogFragment
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.tract.R
import org.cru.godtools.tract.analytics.model.TipAnalyticsScreenEvent
import org.cru.godtools.tract.databinding.TractTipBinding
import org.cru.godtools.xml.model.tips.Tip
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TrainingTipTable
import org.keynote.godtools.android.db.GodToolsDao
import splitties.fragmentargs.arg

@AndroidEntryPoint
class TipBottomSheetDialogFragment() : BaseBottomSheetDialogFragment<TractTipBinding>(), TipCallbacks {
    internal constructor(tip: Tip) : this() {
        tool = tip.manifest.code
        locale = tip.manifest.locale
        this.tip = tip.id
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

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.toolCode.value = tool
        dataModel.locale.value = locale
        dataModel.tipId.value = tip
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.tract_tip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forceExpandedMode()
    }

    override fun onStart() {
        super.onStart()
        if (context?.resources?.getBoolean(R.bool.tract_tips_show_full_height) == true) makeFullScreen()
    }

    override fun onBindingCreated(binding: TractTipBinding, savedInstanceState: Bundle?) {
        binding.callbacks = this
        dataModel.tip.observe(viewLifecycleOwner) { binding.tip = it }
        binding.isComplete = dataModel.isCompleted
        binding.setupPages()
    }

    override fun onDismiss(dialog: DialogInterface) {
        findListener<Callbacks>()?.onDismissTip()
        super.onDismiss(dialog)
    }
    // endregion Lifecycle

    // region Pages
    @Inject
    lateinit var tipPageAdapterFactory: TipPageAdapter.Factory

    private fun TractTipBinding.setupPages() {
        pages.adapter = tipPageAdapterFactory.create(viewLifecycleOwner).also {
            it.callbacks = this@TipBottomSheetDialogFragment
            dataModel.tip.observe(viewLifecycleOwner, it)
        }
        pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = trackScreenAnalytics(position)
        })
    }
    // endregion Pages

    // region TipPageController.Callbacks
    override fun goToNextPage() {
        binding?.apply { pages.currentItem += 1 }
    }

    override fun closeTip(completed: Boolean) {
        if (completed) {
            val trainingTip = TrainingTip(tool, locale, tip)
            trainingTip.isCompleted = true
            GlobalScope.launch { dao.updateOrInsert(trainingTip, TrainingTipTable.COLUMN_IS_COMPLETED) }
        }
        dismissAllowingStateLoss()
    }
    // endregion TipPageController.Callbacks

    private fun trackScreenAnalytics(page: Int = binding?.pages?.currentItem ?: 0) {
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
