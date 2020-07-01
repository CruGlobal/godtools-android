package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentLinkBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Link

internal class LinkController private constructor(
    private val binding: TractContentLinkBinding,
    parentViewHolder: BaseViewHolder<*>?
) : BaseViewHolder<Link>(Link::class.java, binding.root, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }

    fun click(link: Link?) {
        link?.let {
            sendEvents(link.events)
            triggerAnalyticsEvents(link.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        }
    }
}
