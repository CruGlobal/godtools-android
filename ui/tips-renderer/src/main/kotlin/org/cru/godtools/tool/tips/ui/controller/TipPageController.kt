package org.cru.godtools.tool.tips.ui.controller

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import okio.FileSystem
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.renderer.content.RenderContentStack
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.tips.TipPage
import org.cru.godtools.tool.tips.databinding.ToolTipPageBinding
import org.greenrobot.eventbus.EventBus

class TipPageController @AssistedInject internal constructor(
    @Assisted private val binding: ToolTipPageBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    @param:Named(TOOL_RESOURCE_FILE_SYSTEM)
    private val resourceFileSystem: FileSystem,
    private val tipsRepository: TipsRepository,
) : BaseController<TipPage>(TipPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: ToolTipPageBinding, lifecycleOwner: LifecycleOwner, toolState: State): TipPageController
    }

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
        binding.compose.setContent {
            GodToolsTheme(darkTheme = false) {
                ProvideRendererServices(resourceFileSystem, tipsRepository) {
                    RenderContentStack(
                        model?.content.orEmpty(),
                        state = toolState,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

fun ToolTipPageBinding.bindController(
    factory: TipPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, toolState)
