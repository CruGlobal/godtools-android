package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.tract.databinding.TractContentLinkBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Link

internal class LinkController private constructor(
    private val binding: TractContentLinkBinding,
    parentController: BaseController<*>
) : BaseController<Link>(Link::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(TractContentLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<LinkController>

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }

    fun click(link: Link?) {
        triggerAnalyticsEvents(link?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        sendEvents(link?.events)
    }
}
