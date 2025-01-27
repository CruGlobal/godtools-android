package org.cru.godtools.tool.cyoa.ui

import androidx.core.graphics.Insets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import org.ccci.gto.android.common.androidx.lifecycle.filterIsInstance
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.shared.tool.parser.model.page.PageCollectionPage
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPagePageCollectionBinding
import org.cru.godtools.tool.cyoa.ui.controller.PageCollectionPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController

@AndroidEntryPoint
class CyoaPageCollectionPageFragment(page: String? = null) :
    CyoaPageFragment<CyoaPagePageCollectionBinding, PageCollectionPageController>(
        R.layout.cyoa_page_page_collection,
        page
    ) {
    override fun supportsPage(page: Page) = page is PageCollectionPage

    override fun onUpdatePageParams(params: Map<String, String>) {
        super.onUpdatePageParams(params)
        controller?.onUpdatePageParams(params)
    }

    override fun onNewPageEvent(event: Event) = controller?.onNewPageEvent(event) == true

    // region Controller
    @Inject
    internal lateinit var controllerFactory: PageCollectionPageController.Factory

    override fun setupPageController(binding: CyoaPagePageCollectionBinding, insets: StateFlow<Insets>) {
        controller = binding
            .bindController(
                controllerFactory,
                viewLifecycleOwner,
                insets,
                dataModel.enableTips,
                toolState.toolState
            )
            .also { page.filterIsInstance<PageCollectionPage>().observe(viewLifecycleOwner, it) }
            .also { it.callbacks = this }
    }
    // endregion Controller
}
