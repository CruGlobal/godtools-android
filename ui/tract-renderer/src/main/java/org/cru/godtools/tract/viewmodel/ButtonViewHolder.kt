package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import androidx.annotation.UiThread
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractContentButtonBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Button
import org.greenrobot.eventbus.EventBus

@UiThread
internal class ButtonViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Button>(Button::class.java, parent, R.layout.tract_content_button, parentViewHolder) {
    private val binding = TractContentButtonBinding.bind(mRoot).also { it.holder = this }

    public override fun onBind() {
        super.onBind()
        binding.model = mModel
    }

    fun click() {
        val model = mModel ?: return

        triggerAnalyticsEvents(model.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
        when (model.type) {
            Button.Type.URL -> model.url?.let { url ->
                EventBus.getDefault().post(ExitLinkActionEvent(url))
                mRoot.context.openUrl(url)
            }
            Button.Type.EVENT -> sendEvents(model.events)
        }
    }
}
