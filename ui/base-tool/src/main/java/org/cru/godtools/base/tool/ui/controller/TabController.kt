package org.cru.godtools.base.tool.ui.controller

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentTabBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.AnalyticsEvent.Trigger
import org.cru.godtools.tool.model.Tabs.Tab

class TabController @AssistedInject internal constructor(
    @Assisted private val binding: ToolContentTabBinding,
    @Assisted tabsController: TabsController,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Tab>(Tab::class, binding.content, tabsController, cacheFactory) {
    @AssistedFactory
    interface Factory {
        fun create(binding: ToolContentTabBinding, tabsController: TabsController): TabController
    }

    override val childContainer get() = binding.content

    fun trackSelectedAnalyticsEvents() {
        triggerAnalyticsEvents(model?.analyticsEvents, Trigger.SELECTED, Trigger.DEFAULT)
    }
}
