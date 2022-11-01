package org.cru.godtools.tool.cyoa.ui

import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.filterIsInstance
import org.cru.godtools.shared.tool.parser.model.page.CardCollectionPage
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.analytics.model.CyoaCardCollectionPageAnalyticsScreenEvent
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionBinding
import org.cru.godtools.tool.cyoa.ui.controller.CardCollectionPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController

@AndroidEntryPoint
class CyoaCardCollectionPageFragment(
    page: String? = null
) : CyoaPageFragment<CyoaPageCardCollectionBinding, CardCollectionPageController>(
    R.layout.cyoa_page_card_collection,
    page
) {
    override fun supportsPage(page: Page) = page is CardCollectionPage

    // region Controller
    @Inject
    internal lateinit var controllerFactory: CardCollectionPageController.Factory

    override fun setupPageController(binding: CyoaPageCardCollectionBinding) {
        binding.cards.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = triggerAnalyticsScreenView()
        })

        controller =
            binding.bindController(controllerFactory, viewLifecycleOwner, dataModel.enableTips, toolState.toolState)
                .also { page.filterIsInstance<CardCollectionPage>().observe(viewLifecycleOwner, it) }
                .also { it.callbacks = this }
    }
    // endregion Controller

    // region Analytics
    override fun triggerAnalyticsScreenView() {
        val card = controller?.currentCard ?: return
        eventBus.post(CyoaCardCollectionPageAnalyticsScreenEvent(card))
    }
    // endregion Analytics
}
