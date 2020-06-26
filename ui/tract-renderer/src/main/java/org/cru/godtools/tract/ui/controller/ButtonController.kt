package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.UiThread
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.tract.databinding.TractContentButtonBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Button
import org.greenrobot.eventbus.EventBus

@UiThread
internal class ButtonController private constructor(
    private val binding: TractContentButtonBinding,
    parentViewHolder: BaseViewHolder<*>?
) : BaseViewHolder<Button>(binding.root, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }

    fun click() {
        val model = model ?: return
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
