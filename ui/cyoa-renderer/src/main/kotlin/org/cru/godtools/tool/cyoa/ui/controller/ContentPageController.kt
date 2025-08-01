package org.cru.godtools.tool.cyoa.ui.controller

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.graphics.Insets
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.renderer.content.RenderContentStack
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.shared.tool.parser.model.page.ContentPage
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.tool.cyoa.analytics.model.CyoaPageAnalyticsScreenEvent
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.tips.ShowTipCallback
import org.greenrobot.eventbus.EventBus

class ContentPageController @AssistedInject constructor(
    @Assisted private val binding: CyoaPageContentBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted private val contentInsets: StateFlow<Insets>,
    @Assisted override val enableTips: LiveData<Boolean>,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    @param:Named(TOOL_RESOURCE_FILE_SYSTEM)
    private val resourceFileSystem: FileSystem,
    private val tipsRepository: TipsRepository,
) : BaseController<ContentPage>(ContentPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(
            binding: CyoaPageContentBinding,
            lifecycleOwner: LifecycleOwner,
            contentInsets: StateFlow<Insets>,
            enableTips: LiveData<Boolean>,
            toolState: State
        ): ContentPageController
    }

    internal var callbacks: ShowTipCallback? = null

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.contentInsets = contentInsets
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
        binding.compose.setContent {
            val insets by contentInsets.collectAsState()

            ProvideRendererServices(resourceFileSystem, tipsRepository = tipsRepository) {
                RenderContentStack(
                    model?.content.orEmpty(),
                    modifier = Modifier
                        .padding(WindowInsets(top = insets.top).asPaddingValues()),
                    state = toolState,
                )
            }
        }
    }

    // region Analytics Events
    private var pendingVisibleAnalyticsEvents: List<Job>? = null

    init {
        with(lifecycleOwner.lifecycle) {
            onResume {
                model?.let { eventBus.post(CyoaPageAnalyticsScreenEvent(it)) }
                pendingVisibleAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE))
            }
            onPause {
                pendingVisibleAnalyticsEvents?.cancelPendingAnalyticsEvents()
                triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.HIDDEN))
            }
        }
    }
    // endregion Analytics Events

    // region Tips
    override fun showTip(tip: Tip?) {
        tip?.let { callbacks?.showTip(tip) }
    }
    // endregion Tips
}

fun CyoaPageContentBinding.bindController(
    factory: ContentPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    contentInsets: StateFlow<Insets>,
    enableTips: LiveData<Boolean>,
    toolState: State
) = controller ?: factory.create(this, lifecycleOwner, contentInsets, enableTips, toolState)

fun CyoaPageContentBinding.bindController(
    factory: ContentPageController.Factory,
    parentController: BaseController<*>,
    contentInsets: StateFlow<Insets>,
) = controller ?: factory.create(
    this,
    parentController.lifecycleOwner,
    contentInsets,
    parentController.enableTips,
    parentController.toolState
)
