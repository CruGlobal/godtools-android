package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractContentTabBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Tab

class TabController internal constructor(private val binding: TractContentTabBinding, tabsController: TabsController) :
    ParentController<Tab>(Tab::class, binding.content, tabsController) {
    override val contentContainer get() = binding.content

    fun trackSelectedAnalyticsEvents() {
        model?.let { triggerAnalyticsEvents(it.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT) }
    }
}

// TODO: this may change once I figure out what code pattern I want to use to create/bind controllers
internal fun TractContentTabBinding.bindController(tabsController: TabsController) =
    BaseViewHolder.forView(root, TabController::class.java) ?: TabController(this, tabsController)
