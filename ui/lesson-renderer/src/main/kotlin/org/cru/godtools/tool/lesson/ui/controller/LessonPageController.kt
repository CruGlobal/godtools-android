package org.cru.godtools.tool.lesson.ui.controller

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import okio.FileSystem
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.renderer.lesson.LessonPageEvent
import org.cru.godtools.shared.renderer.lesson.RenderLessonPage
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.tool.lesson.ui.LessonPageAdapter
import org.greenrobot.eventbus.EventBus

class LessonPageController @AssistedInject constructor(
    @Assisted private val binding: LessonPageBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    @Assisted private val callbacks: LessonPageAdapter.Callbacks?,
    eventBus: EventBus,
    @param:Named(TOOL_RESOURCE_FILE_SYSTEM)
    private val resourceFileSystem: FileSystem,
    private val tipsRepository: TipsRepository,
) : BaseController<LessonPage>(LessonPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(
            binding: LessonPageBinding,
            lifecycleOwner: LifecycleOwner,
            toolState: State,
            callbacks: LessonPageAdapter.Callbacks?,
        ): LessonPageController
    }

    init {
        binding.root.setViewTreeLifecycleOwner(lifecycleOwner)
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.compose.setContent {
            ProvideRendererServices(resources = resourceFileSystem, tipsRepository = tipsRepository) {
                model?.let {
                    RenderLessonPage(it, state = toolState) {
                        when (it) {
                            LessonPageEvent.NextPage -> callbacks?.goToNextPage()
                            LessonPageEvent.PreviousPage -> callbacks?.goToPreviousPage()
                        }
                    }
                }
            }
        }
    }
}

fun LessonPageBinding.bindController(
    factory: LessonPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
    callbacks: LessonPageAdapter.Callbacks?,
) = controller ?: factory.create(this, lifecycleOwner, toolState, callbacks)
