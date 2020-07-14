package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentLinkBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Link

internal class LinkController private constructor(
    private val binding: TractContentLinkBinding,
    parentController: BaseController<*>?
) : BaseController<Link>(Link::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentController: BaseController<*>?) :
        this(TractContentLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

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
