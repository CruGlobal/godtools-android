package org.cru.godtools.tool.cyoa.ui.settings

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.ui.settings.ToolOptionsSettingsBottomSheetDialogFragment
import org.cru.godtools.tool.databinding.ToolOptionSheetBinding

@AndroidEntryPoint
class CyoaSettingsBottomSheetDialogFragment : ToolOptionsSettingsBottomSheetDialogFragment() {

    override fun onBindingCreated(binding: ToolOptionSheetBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.hasShareLink = false
    }

    override fun shareLink() {}

    override fun shareScreen() {}
}
