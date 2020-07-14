package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.tract.databinding.TractContentButtonBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Button

internal class ButtonController private constructor(
    private val binding: TractContentButtonBinding,
    parentController: BaseController<*>?
) : BaseController<Button>(Button::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
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
