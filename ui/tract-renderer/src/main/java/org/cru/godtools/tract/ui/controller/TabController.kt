package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentParagraphBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Tab

class TabController private constructor(
    private val binding: TractContentParagraphBinding,
    parentViewHolder: BaseViewHolder<*>?
) : ParentController<Tab>(binding.content, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentParagraphBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    override val contentContainer get() = binding.content

    fun trackSelectedAnalyticsEvents() {
        model?.let { triggerAnalyticsEvents(it.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT) }
    }
}
