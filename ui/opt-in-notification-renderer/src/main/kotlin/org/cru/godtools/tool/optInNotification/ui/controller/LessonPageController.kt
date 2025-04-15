package org.cru.godtools.tool.optInNotification.ui.controller

import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.shared.tool.state.State
import org.cru.godtools.tool.optInNotification.databinding.LessonPageBinding
import org.greenrobot.eventbus.EventBus

class LessonPageController @AssistedInject constructor(
    @Assisted private val binding: LessonPageBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus
) : ParentController<LessonPage>(LessonPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
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
    }

    override val childContainer get() = binding.content
}

fun LessonPageBinding.bindController(
    factory: LessonPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, toolState)
