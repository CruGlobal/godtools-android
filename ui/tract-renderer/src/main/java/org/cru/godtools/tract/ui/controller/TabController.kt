package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractContentTabBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Tab

class TabController internal constructor(private val binding: TractContentTabBinding, tabsController: TabsController) :
    ParentController<Tab>(Tab::class, binding.content, tabsController) {
    init {
        binding.controller = this
    }

    override val contentContainer get() = binding.content

    fun trackSelectedAnalyticsEvents() {
        triggerAnalyticsEvents(model?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
    }
}

internal fun TractContentTabBinding.bindController(tabsController: TabsController) =
    controller ?: TabController(this, tabsController)
