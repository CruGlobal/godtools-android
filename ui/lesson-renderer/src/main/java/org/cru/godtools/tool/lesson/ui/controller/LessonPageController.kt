package org.cru.godtools.tool.lesson.ui.controller

import androidx.lifecycle.Lifecycle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.tool.model.AnalyticsEvent.Trigger
import org.cru.godtools.tool.model.lesson.LessonPage
import org.cru.godtools.tool.state.State
import org.greenrobot.eventbus.EventBus

class LessonPageController @AssistedInject constructor(
    @Assisted private val binding: LessonPageBinding,
    @Assisted override val toolState: State,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus
) : ParentController<LessonPage>(LessonPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: LessonPageBinding, toolState: State): LessonPageController
    }

    override val lifecycleOwner = binding.lifecycleOwner
        ?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }
        ?.also { binding.lifecycleOwner = it }

    private var pendingVisibleAnalyticsEvents: List<Job>? = null

    init {
        binding.controller = this

        lifecycleOwner?.lifecycle?.apply {
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

fun LessonPageBinding.bindController(factory: LessonPageController.Factory, toolState: State) =
    controller ?: factory.create(this, toolState)
