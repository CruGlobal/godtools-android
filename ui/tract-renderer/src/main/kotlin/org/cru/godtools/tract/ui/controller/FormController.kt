package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.tool.model.Form
import org.cru.godtools.tract.databinding.TractContentFormBinding

class FormController private constructor(
    private val binding: TractContentFormBinding,
    parentController: BaseController<*>,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Form>(Form::class, binding.content, parentController, cacheFactory) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : this(
        TractContentFormBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        cacheFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<FormController>

    override val childContainer get() = binding.content

    override fun validate(ids: List<EventId>): Boolean {
        // XXX: right now we only validate if we have a followup:send event
        if (EventId.FOLLOWUP in ids) return onValidate()

        // default to default validation logic
        return super.validate(ids)
    }

    override fun buildEvent(builder: Event.Builder): Boolean {
        // we override the default event building process
        onBuildEvent(builder, true)
        return true
    }
}
