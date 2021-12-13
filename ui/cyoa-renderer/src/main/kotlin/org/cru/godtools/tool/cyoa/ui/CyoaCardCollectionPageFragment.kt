package org.cru.godtools.tool.cyoa.ui

import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionBinding
import org.cru.godtools.tool.cyoa.ui.controller.CardCollectionPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController
import org.cru.godtools.tool.model.page.CardCollectionPage

@AndroidEntryPoint
class CyoaCardCollectionPageFragment(page: String? = null) :
    CyoaPageFragment<CyoaPageCardCollectionBinding>(R.layout.cyoa_page_card_collection, page) {
    // region Controller
    @Inject
    internal lateinit var controllerFactory: CardCollectionPageController.Factory

    override fun setupPageController(binding: CyoaPageCardCollectionBinding) {
        controller = binding.bindController(controllerFactory, viewLifecycleOwner, toolState.toolState)
            .also { page.map { it as? CardCollectionPage }.observe(viewLifecycleOwner, it) }
    }
    // endregion Controller
}
