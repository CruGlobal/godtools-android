package org.cru.godtools.tool.cyoa.ui

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.filterIsInstance
import org.cru.godtools.shared.tool.parser.model.page.ContentPage
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.analytics.model.CyoaPageAnalyticsScreenEvent
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.cyoa.ui.controller.ContentPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController

@AndroidEntryPoint
class CyoaContentPageFragment(
    page: String? = null
) : CyoaPageFragment<CyoaPageContentBinding, ContentPageController>(R.layout.cyoa_page_content, page) {
    override fun supportsPage(page: Page) = page is ContentPage

    // region Controller
    @Inject
    internal lateinit var controllerFactory: ContentPageController.Factory

    override fun setupPageController(binding: CyoaPageContentBinding) {
        controller = binding.bindController(controllerFactory, dataModel.enableTips, toolState.toolState)
            .also { page.filterIsInstance<ContentPage>().observe(viewLifecycleOwner, it) }
            .also { it.callbacks = this }
    }
    // endregion Controller

    // region Analytics
    override fun triggerAnalyticsScreenView() {
        val page = page.value ?: return
        eventBus.post(CyoaPageAnalyticsScreenEvent(page))
    }
    // endregion Analytics
}
