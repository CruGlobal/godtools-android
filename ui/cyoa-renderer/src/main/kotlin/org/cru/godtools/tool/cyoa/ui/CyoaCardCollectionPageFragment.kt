package org.cru.godtools.tool.cyoa.ui

import androidx.lifecycle.map
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.analytics.model.CyoaCardCollectionPageAnalyticsScreenEvent
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionBinding
import org.cru.godtools.tool.cyoa.ui.controller.CardCollectionPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController
import org.cru.godtools.tool.model.page.CardCollectionPage
import org.cru.godtools.tool.model.page.Page

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
        controller = binding.bindController(controllerFactory, viewLifecycleOwner, toolState.toolState).also {
            page.map { it as? CardCollectionPage }.observe(viewLifecycleOwner, it)
            binding.cards.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = triggerAnalyticsScreenView()
            })
        }
    }
    // endregion Controller

    // region Analytics
    override fun triggerAnalyticsScreenView() {
        val card = controller?.currentCard ?: return
        eventBus.post(CyoaCardCollectionPageAnalyticsScreenEvent(card))
    }
    // endregion Analytics
}
