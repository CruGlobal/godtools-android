package org.cru.godtools.tract.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.UiControllerCache
import org.cru.godtools.tract.databinding.TractContentTabBinding
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger
import org.cru.godtools.xml.model.Tab

class TabController @AssistedInject internal constructor(
    @Assisted private val binding: TractContentTabBinding,
    @Assisted tabsController: TabsController,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Tab>(Tab::class, binding.content, tabsController, cacheFactory) {
    @AssistedFactory
    interface Factory {
        fun create(binding: TractContentTabBinding, tabsController: TabsController): TabController
    }

    override val contentContainer get() = binding.content

    fun trackSelectedAnalyticsEvents() {
        triggerAnalyticsEvents(model?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
    }
}
