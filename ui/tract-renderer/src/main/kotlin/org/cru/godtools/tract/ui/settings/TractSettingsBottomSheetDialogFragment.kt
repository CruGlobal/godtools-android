package org.cru.godtools.tract.ui.settings

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.ui.settings.ToolOptionsSettingsBottomSheetDialogFragment
import org.cru.godtools.tool.databinding.ToolOptionSheetBinding
import org.cru.godtools.tract.activity.TractActivity

@AndroidEntryPoint
class TractSettingsBottomSheetDialogFragment : ToolOptionsSettingsBottomSheetDialogFragment() {

    override fun onBindingCreated(binding: ToolOptionSheetBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.hasShareLink = true
    }

    // region TractSettingsSheetCallbacks
    override fun shareLink() {
        (activity as? BaseToolActivity<*>)?.shareCurrentTool()
        dismissAllowingStateLoss()
    }

    override fun shareScreen() {
        (activity as? TractActivity)?.shareLiveShareLink()
        dismissAllowingStateLoss()
    }
    // endregion TractSettingsSheetCallbacks
}
