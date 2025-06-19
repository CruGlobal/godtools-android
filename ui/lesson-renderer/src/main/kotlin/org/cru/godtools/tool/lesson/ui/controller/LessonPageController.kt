package org.cru.godtools.tool.lesson.ui.controller

import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import kotlinx.coroutines.Job
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.renderer.content.RenderContentStack
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.greenrobot.eventbus.EventBus

class LessonPageController @AssistedInject constructor(
    @Assisted private val binding: LessonPageBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    @Named(TOOL_RESOURCE_FILE_SYSTEM)
    private val resourceFileSystem: FileSystem,
) : BaseController<LessonPage>(LessonPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: LessonPageBinding, lifecycleOwner: LifecycleOwner, toolState: State): LessonPageController
    }

    private var pendingVisibleAnalyticsEvents: List<Job>? = null

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this

        with(lifecycleOwner.lifecycle) {
            onResume {
                pendingVisibleAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE))
            }
            onPause { pendingVisibleAnalyticsEvents?.cancelPendingAnalyticsEvents() }
        }
    }

    override fun onBind() {
        super.onBind()
        binding.model = model
        binding.compose.setContent {
            ProvideRendererServices(resourceFileSystem) {
                RenderContentStack(
                    model?.content.orEmpty(),
                    state = toolState,
                )
            }
        }
    }
}

fun LessonPageBinding.bindController(
    factory: LessonPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, toolState)
