package org.cru.godtools.tool.tips.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.fluidsonic.locale.toPlatform
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import okio.FileSystem
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.renderer.tips.RenderTip
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.tool.tips.R
import splitties.fragmentargs.arg

@AndroidEntryPoint
class TipBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        fun create(tip: Tip): TipBottomSheetDialogFragment? = TipBottomSheetDialogFragment().apply {
            tool = tip.manifest.code ?: return null
            locale = tip.manifest.locale?.toPlatform() ?: return null
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
    @Named(TOOL_RESOURCE_FILE_SYSTEM)
    internal lateinit var resourceFileSystem: FileSystem
    @Inject
    internal lateinit var tipsRepository: TipsRepository

    private val dataModel: TipBottomSheetDialogFragmentDataModel by viewModels()
    private val toolState: ToolStateHolder by activityViewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.toolCode.value = tool
        dataModel.locale.value = locale
        dataModel.tipId.value = tip
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GodToolsTheme(darkTheme = false) {
                    ProvideRendererServices(resourceFileSystem, tipsRepository) {
                        val tip by dataModel.tip.collectAsState()
                        val sizeModifier = when {
                            booleanResource(R.bool.tool_tips_show_full_height) -> Modifier.fillMaxSize()
                            else -> Modifier.fillMaxWidth().height(450.dp)
                        }
                        tip?.let {
                            RenderTip(
                                it,
                                state = toolState.toolState,
                                onDismiss = { dismissAllowingStateLoss() },
                                modifier = Modifier
                                    .nestedScroll(rememberNestedScrollInteropConnection())
                                    .then(sizeModifier)
                            )
                        }
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forceExpandedMode()
    }

    override fun onStart() {
        super.onStart()
        if (context?.resources?.getBoolean(R.bool.tool_tips_show_full_height) == true) makeFullScreen()
    }

    override fun onDismiss(dialog: DialogInterface) {
        findListener<Callbacks>()?.onDismissTip()
        super.onDismiss(dialog)
    }
    // endregion Lifecycle

    private fun forceExpandedMode() {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun makeFullScreen() {
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            layoutParams = layoutParams.apply { height = ViewGroup.LayoutParams.MATCH_PARENT }
        }
    }
}

@HiltViewModel
internal class TipBottomSheetDialogFragmentDataModel @Inject constructor(manifestManager: ManifestManager) :
    LatestPublishedManifestDataModel(manifestManager) {
    val tipId = MutableStateFlow<String?>(null)

    val tip = manifestFlow.combine(tipId) { m, t -> m?.findTip(t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
