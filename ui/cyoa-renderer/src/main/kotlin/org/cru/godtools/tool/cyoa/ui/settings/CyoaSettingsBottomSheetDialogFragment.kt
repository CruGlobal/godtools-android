package org.cru.godtools.tool.cyoa.ui.settings

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.ui.settings.SettingsBottomSheetDialogFragment
import org.cru.godtools.tool.databinding.ToolSettingsSheetBinding

@AndroidEntryPoint
class CyoaSettingsBottomSheetDialogFragment : SettingsBottomSheetDialogFragment() {

    override fun onBindingCreated(binding: ToolSettingsSheetBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.hasShareLink = false
    }

    override fun shareLink() {}

    override fun shareScreen() {}
}
