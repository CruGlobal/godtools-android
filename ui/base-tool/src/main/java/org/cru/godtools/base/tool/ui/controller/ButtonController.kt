package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.tool.BR
import org.cru.godtools.base.tool.databinding.ToolContentButtonBinding
import org.cru.godtools.base.tool.databinding.ToolContentButtonOutlinedBinding
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Button

internal sealed class ButtonController<T : ViewDataBinding>(
    private val binding: T,
    parentController: BaseController<*>
) : BaseController<Button>(Button::class, binding.root, parentController) {
    init {
        binding.setVariable(BR.controller, this)
    }

    public override fun onBind() {
        super.onBind()
        binding.setVariable(BR.model, model)
    }

    fun click() {
        triggerAnalyticsEvents(model?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        when (model?.type) {
            Button.Type.URL -> model?.url?.let { url ->
                eventBus.post(ExitLinkActionEvent(url))
                root.context.openUrl(url)
            }
            Button.Type.EVENT -> sendEvents(model?.events)
        }
    }
}

internal class ContainedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>
) : ButtonController<ToolContentButtonBinding>(
    ToolContentButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<ContainedButtonController>
}

internal class OutlinedButtonController @AssistedInject internal constructor(
    @Assisted parent: ViewGroup,
    @Assisted parentController: BaseController<*>
) : ButtonController<ToolContentButtonOutlinedBinding>(
    ToolContentButtonOutlinedBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    parentController
) {
    @AssistedFactory
    interface Factory : BaseController.Factory<OutlinedButtonController>
}
