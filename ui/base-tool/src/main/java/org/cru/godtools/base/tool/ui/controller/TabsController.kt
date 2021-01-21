package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.material.tabs.setBackgroundTint
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.databinding.ToolContentTabsBinding
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.primaryColor

class TabsController private constructor(
    private val binding: ToolContentTabsBinding,
    parentController: BaseController<*>,
    tabControllerFactory: TabController.Factory
) : BaseController<Tabs>(Tabs::class, binding.root, parentController), OnTabSelectedListener {
    @AssistedInject
    constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        tabControllerFactory: TabController.Factory
    ) : this(
        ToolContentTabsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        tabControllerFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<TabsController>

    private val tabController = tabControllerFactory.create(binding.tab, this)

    init {
        binding.tabs.addOnTabSelectedListener(this)
    }

    // region Lifecycle
    @UiThread
    override fun onBind() {
        super.onBind()
        binding.model = model
        bindTabs()
        bindTab()
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        checkForTabEvent(event)
        tabController.onContentEvent(event)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        bindTab(tab.position)
        if (!bindingTabs) tabController.trackSelectedAnalyticsEvents()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    override fun onTabReselected(tab: TabLayout.Tab) = Unit
    // endregion Lifecycle

    private var bindingTabs = false
    private fun bindTabs() {
        bindingTabs = true

        // update tabs for the TabLayout
        val primaryColor = model?.stylesParent.primaryColor
        binding.tabs.removeAllTabs()
        model?.tabs?.forEach {
            binding.tabs.apply {
                val tab = newTab().apply {
                    setBackgroundTint(primaryColor)
                    text = it.label?.text
                }
                addTab(tab)
            }
        }

        bindingTabs = false
    }

    private fun bindTab(index: Int = binding.tabs.selectedTabPosition) {
        tabController.model = model?.tabs?.getOrNull(index)
    }

    private fun checkForTabEvent(event: Event) {
        model?.tabs?.firstOrNull { it.listeners.contains(event.id) }
            ?.let { binding.tabs.getTabAt(it.position) }
            ?.select()
    }
}
