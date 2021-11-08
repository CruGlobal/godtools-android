package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.cyoa.ui.controller.ContentPageController
import org.cru.godtools.tool.cyoa.ui.controller.bindController
import org.cru.godtools.tool.model.page.ContentPage
import splitties.fragmentargs.arg

@AndroidEntryPoint
class CyoaPageFragment() : BaseFragment<CyoaPageContentBinding>(R.layout.cyoa_page_content) {
    constructor(page: String) : this() {
        this.pageId = page
    }

    internal var pageId by arg<String>()
        private set

    private val dataModel by activityViewModels<MultiLanguageToolActivityDataModel>()
    private val toolState by activityViewModels<ToolStateHolder>()
    private val pageInsets by activityViewModels<PageInsets>()

    // region Lifecycle
    override fun onBindingCreated(binding: CyoaPageContentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.contentInsets = pageInsets.insets
        setupPageController(binding)
    }

    internal fun onContentEvent(event: Event) {
        controller?.onContentEvent(event)
    }

    override fun onDestroyBinding(binding: CyoaPageContentBinding) {
        cleanupPageController()
    }
    // endregion Lifecycle

    // region Page
    internal val page by lazy { dataModel.activeManifest.map { it?.findPage(pageId) } }

    @Inject
    internal lateinit var controllerFactory: ContentPageController.Factory
    private var controller: ContentPageController? = null

    private fun setupPageController(binding: CyoaPageContentBinding) {
        controller = binding.bindController(controllerFactory, toolState.toolState)
            .also { page.map { it as? ContentPage }.observe(viewLifecycleOwner, it) }
    }

    private fun cleanupPageController() {
        controller = null
    }
    // endregion Page
}
