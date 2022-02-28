package org.cru.godtools.base.tool.ui.share

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.material.bottomsheet.BindingBottomSheetDialogFragment
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.databinding.ToolShareSheetBinding
import org.cru.godtools.base.tool.ui.share.model.ShareItem
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull

internal class ShareBottomSheetDialogFragment() :
    BindingBottomSheetDialogFragment<ToolShareSheetBinding>(R.layout.tool_share_sheet),
    ShareAppsAdapter.Callbacks,
    OtherActionsAdapter.Callbacks {
    constructor(shareItems: List<ShareItem>) : this() {
        primaryShareItem = shareItems.firstOrNull()?.takeIf { it.shareIntent != null }
        otherShareItems = if (primaryShareItem != null) shareItems.drop(1) else shareItems
    }

    private var primaryShareItem by argOrNull<ShareItem>()
    private var otherShareItems by arg<List<ShareItem>>()

    override fun onBindingCreated(binding: ToolShareSheetBinding, savedInstanceState: Bundle?) {
        binding.primaryShareItem = primaryShareItem
        binding.otherShareItems = otherShareItems

        primaryShareItem?.shareIntent?.let { intent ->
            viewLifecycleOwner.lifecycleScope.launch {
                val items = withContext(Dispatchers.IO) {
                    requireContext().packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY).take(6)
                }
                binding.apps.adapter = ShareAppsAdapter(items, this@ShareBottomSheetDialogFragment)
            }
        }
        binding.otherActions.adapter = OtherActionsAdapter(this, otherShareItems, this)
    }

    // region ResolveInfoAdapter.Callbacks
    override fun onOpenApp(info: ResolveInfo) {
        primaryShareItem?.shareIntent?.let { Intent(it) }?.let { intent ->
            intent.component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
            intent.setPackage(info.activityInfo.packageName)
            startActivity(intent)
        }
        dismissAllowingStateLoss()
    }

    override fun onShowChooser() {
        val shareItem = primaryShareItem
        shareItem?.shareIntent?.let { startActivity(Intent.createChooser(it, null)) }
        dismissAllowingStateLoss()
    }
    // endregion ResolveInfoAdapter.Callbacks

    // region OtherActionsAdapter.Callbacks
    override fun triggerAction(item: ShareItem?) {
        activity?.let { item?.triggerAction(it) }
        dismissAllowingStateLoss()
    }
    // endregion OtherActionsAdapter.Callbacks
}
