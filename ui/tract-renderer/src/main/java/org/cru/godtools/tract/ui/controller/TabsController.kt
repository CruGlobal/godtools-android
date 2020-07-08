package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.ccci.gto.android.common.material.tabs.setBackgroundTint
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractContentTabsBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.primaryColor

class TabsController private constructor(
    private val binding: TractContentTabsBinding,
    parentViewHolder: BaseViewHolder<*>?
) : BaseViewHolder<Tabs>(Tabs::class.java, binding.root, parentViewHolder), OnTabSelectedListener {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentTabsBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    private val tabController = binding.tab.bindController(this)

    init {
        binding.tabs.addOnTabSelectedListener(this)
    }

    // region Lifecycle
    @UiThread
    override fun onBind() {
        super.onBind()
        binding.model = model
        bindTabs()
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        checkForTabEvent(event)
        tabController.onContentEvent(event)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tabController.bind(model?.tabs?.getOrNull(tab.position))
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
                addTab(newTab().apply {
                    setBackgroundTint(primaryColor)
                    text = it.label?.text
                })
            }
        }

        bindingTabs = false
    }

    private fun checkForTabEvent(event: Event) {
        model?.tabs?.firstOrNull { it.listeners.contains(event.id) }
            ?.let { binding.tabs.getTabAt(it.position) }
            ?.select()
    }
}
