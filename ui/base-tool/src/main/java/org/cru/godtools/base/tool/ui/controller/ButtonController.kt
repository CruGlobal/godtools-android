package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.tool.BR
import org.cru.godtools.base.tool.databinding.ToolContentButtonBinding
import org.cru.godtools.base.tool.databinding.ToolContentButtonOutlinedBinding
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.tool.model.AnalyticsEvent.Trigger
import org.cru.godtools.tool.model.Base
import org.cru.godtools.tool.model.Button
import org.greenrobot.eventbus.EventBus

internal sealed class ButtonController<T : ViewDataBinding>(
    private val binding: T,
    parentController: BaseController<*>,
    eventBus: EventBus
) : BaseController<Button>(Button::class, binding.root, parentController, eventBus) {
    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.setVariable(BR.controller, this)
    }

    public override fun onBind() {
        super.onBind()
        binding.setVariable(BR.model, model)
        binding.setVariable(BR.isGone, model?.isGoneFlow(toolState)?.asLiveData())
        binding.setVariable(BR.isInvisible, model?.isInvisibleFlow(toolState)?.asLiveData())
    }

    fun click() {
        val model = model
        triggerAnalyticsEvents(model?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        when (model?.type) {
            Button.Type.URL -> model.url?.let { url ->
                eventBus.post(ExitLinkActionEvent(model.manifest.code, url))
                root.context.openUrl(url)
            }
            Button.Type.EVENT -> sendEvents(model.events)
            Button.Type.UNKNOWN -> Unit
        }
    }
}

internal class ContainedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>,
    eventBus: EventBus
) : ButtonController<ToolContentButtonBinding>(
    ToolContentButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController,
    eventBus
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<ContainedButtonController>

    override fun supportsModel(model: Base) =
        super.supportsModel(model) && model is Button && model.style == Button.Style.CONTAINED
}

internal class OutlinedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>,
    eventBus: EventBus
) : ButtonController<ToolContentButtonOutlinedBinding>(
    ToolContentButtonOutlinedBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController,
    eventBus
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<OutlinedButtonController>

    override fun supportsModel(model: Base) =
        super.supportsModel(model) && model is Button && model.style == Button.Style.OUTLINED
}
