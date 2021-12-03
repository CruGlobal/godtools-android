package org.cru.godtools.tool.cyoa.ui

import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.cyoa.ui.controller.ContentPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController
import org.cru.godtools.tool.model.page.ContentPage

@AndroidEntryPoint
class CyoaContentPageFragment(page: String? = null) :
    CyoaPageFragment<CyoaPageContentBinding>(R.layout.cyoa_page_content, page) {
    // region Controller
    @Inject
    internal lateinit var controllerFactory: ContentPageController.Factory

    override fun setupPageController(binding: CyoaPageContentBinding) {
        controller = binding.bindController(controllerFactory, toolState.toolState)
            .also { page.map { it as? ContentPage }.observe(viewLifecycleOwner, it) }
    }
    // endregion Controller
}
