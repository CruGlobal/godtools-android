package org.cru.godtools.tract.ui.settings

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.ui.settings.SettingsBottomSheetDialogFragment
import org.cru.godtools.tool.databinding.ToolSettingsSheetBinding
import org.cru.godtools.tract.activity.TractActivity

@AndroidEntryPoint
class TractSettingsBottomSheetDialogFragment : SettingsBottomSheetDialogFragment() {

    override fun onBindingCreated(binding: ToolSettingsSheetBinding, savedInstanceState: Bundle?) {
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
