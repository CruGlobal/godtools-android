package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import butterknife.BindView
import butterknife.OnClick
import org.cru.godtools.base.tool.model.view.bindTo
import org.cru.godtools.tract.R
import org.cru.godtools.tract.R2
import org.cru.godtools.xml.model.AnalyticsEvent
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.primaryColor
import org.cru.godtools.xml.model.stylesParent

@UiThread
internal class LinkViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Link>(Link::class.java, parent, R.layout.tract_content_link, parentViewHolder) {
    @BindView(R2.id.content_link)
    lateinit var link: TextView

    // region Lifecycle Events
    public override fun onBind() {
        super.onBind()
        bindText()
    }

    // endregion Lifecycle Events
    private fun bindText() {
        mModel?.text.bindTo(link, null, mModel.stylesParent.primaryColor)
    }

    @OnClick(R2.id.content_link)
    fun click() {
        mModel?.let { model ->
            sendEvents(model.events)
            triggerAnalyticsEvents(model.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        }
    }
}
