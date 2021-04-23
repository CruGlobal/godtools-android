package org.cru.godtools.tool.lesson.ui.controller

import androidx.lifecycle.Lifecycle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.xml.model.lesson.LessonPage
import org.greenrobot.eventbus.EventBus

class LessonPageController @AssistedInject constructor(
    @Assisted private val binding: LessonPageBinding,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus
) : ParentController<LessonPage>(LessonPage::class, binding.root, cacheFactory = cacheFactory, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(binding: LessonPageBinding): LessonPageController
    }

    init {
        binding.controller = this
    }

    override val lifecycleOwner = binding.lifecycleOwner
        ?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }
        ?.also { binding.lifecycleOwner = it }

    override fun onBind() {
        super.onBind()
        binding.model = model
    }

    override val contentContainer get() = binding.content
}

fun LessonPageBinding.bindController(factory: LessonPageController.Factory) = controller ?: factory.create(this)
