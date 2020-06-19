package org.cru.godtools.tract.viewmodel

import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.view.ViewCompat
import butterknife.BindView
import butterknife.OnClick
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.tool.model.view.bindTo
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.tract.R
import org.cru.godtools.tract.R2
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.buttonColor
import org.greenrobot.eventbus.EventBus

@UiThread
internal class ButtonViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Button>(Button::class.java, parent, R.layout.tract_content_button, parentViewHolder) {

    @BindView(R2.id.button)
    internal lateinit var mButton: TextView

    // region Lifecycle Events
    public override fun onBind() {
        super.onBind()
        mModel?.text.bindTo(mButton)
        ViewCompat.setBackgroundTintList(mButton, ColorStateList.valueOf(mModel.buttonColor))
    }
    // endregion Lifecycle Events

    @OnClick(R2.id.button)
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
