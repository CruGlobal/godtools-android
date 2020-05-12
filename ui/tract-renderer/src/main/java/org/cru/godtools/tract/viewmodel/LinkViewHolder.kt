package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import androidx.annotation.UiThread
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractContentLinkBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Link

@UiThread
internal class LinkViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Link>(Link::class.java, parent, R.layout.tract_content_link, parentViewHolder) {
    private val binding = TractContentLinkBinding.bind(mRoot).also { it.holder = this }

    public override fun onBind() {
        super.onBind()
        binding.model = mModel
    }

    fun click(link: Link?) {
        link?.let {
            sendEvents(link.events)
            triggerAnalyticsEvents(link.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        }
    }
}
