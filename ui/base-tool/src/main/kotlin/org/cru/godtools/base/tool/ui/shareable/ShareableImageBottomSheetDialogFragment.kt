package org.cru.godtools.base.tool.ui.shareable

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.material.bottomsheet.BindingBottomSheetDialogFragment
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.analytics.model.ShareShareableAnalyticsActionEvent
import org.cru.godtools.base.tool.databinding.ToolShareableImageSheetBinding
import org.cru.godtools.base.tool.model.shareable.buildShareIntent
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg

@AndroidEntryPoint
class ShareableImageBottomSheetDialogFragment() :
    BindingBottomSheetDialogFragment<ToolShareableImageSheetBinding>(R.layout.tool_shareable_image_sheet) {
    constructor(tool: String, locale: Locale, shareableId: String) : this() {
        this.tool = tool
        this.locale = locale
        this.shareableId = shareableId
    }

    private var tool by arg<String>()
    private var locale by arg<Locale>()
    private var shareableId by arg<String>()

    private val dataModel: ShareableImageBottomSheetDialogFragmentDataModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.tool.value = tool
        dataModel.locale.value = locale
        dataModel.shareableId.value = shareableId
    }

    override fun onBindingCreated(binding: ToolShareableImageSheetBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.shareable = dataModel.shareable
        binding.actionShare.setOnClickListener { shareShareable() }
    }
    // endregion Lifecycle

    @Inject
    internal lateinit var eventBus: EventBus
    @Inject
    internal lateinit var toolFileSystem: ToolFileSystem

    private fun shareShareable() {
        val context = context ?: return
        val shareable = dataModel.shareable.value ?: return
        val intent = shareable.buildShareIntent(context) ?: return
        eventBus.post(ShareShareableAnalyticsActionEvent(shareable))
        startActivity(Intent.createChooser(intent, null))
        dismissAllowingStateLoss()
    }
}
