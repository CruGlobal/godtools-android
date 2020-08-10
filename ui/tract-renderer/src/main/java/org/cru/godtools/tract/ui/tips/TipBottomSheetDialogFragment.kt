package org.cru.godtools.tract.ui.tips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.fragment.BaseBottomSheetDialogFragment
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractTipBinding
import org.cru.godtools.tract.ui.controller.tips.TipPageController
import org.cru.godtools.xml.model.tips.Tip
import splitties.fragmentargs.arg
import java.util.Locale
import javax.inject.Inject

class TipBottomSheetDialogFragment() : BaseBottomSheetDialogFragment<TractTipBinding>(), TipPageController.Callbacks {
    internal constructor(tip: Tip) : this() {
        tool = tip.manifest.code
        locale = tip.manifest.locale
        this.tip = tip.id
    }

    private var tool: String by arg()
    private var locale: Locale by arg()
    private var tip: String by arg()

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
        dataModel.tip.observe(viewLifecycleOwner) { binding.tip = it }
        binding.setupPages()
    }
    // endregion Lifecycle

    // region Pages
    @Inject
    lateinit var tipPageAdapterFactory: TipPageAdapter.Factory
    private val tipPageAdapter by lazy {
        tipPageAdapterFactory.create(this).also {
            it.callbacks = this
            dataModel.tip.observe(this, it)
        }
    }

    private fun TractTipBinding.setupPages() {
        pages.adapter = tipPageAdapter
    }
    // endregion Pages

    // region TipPageController.Callbacks
    override fun goToNextPage() {
        binding?.apply { pages.currentItem += 1 }
    }

    override fun closeTip() {
        dismissAllowingStateLoss()
    }
    // endregion TipPageController.Callbacks

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

internal class TipBottomSheetDialogFragmentDataModel @Inject constructor(manifestManager: ManifestManager) :
    LatestPublishedManifestDataModel(manifestManager) {
    val tipId = MutableLiveData<String>()

    val tip = manifest.combineWith(tipId.distinctUntilChanged()) { m, t -> m?.findTip(t) }
}
