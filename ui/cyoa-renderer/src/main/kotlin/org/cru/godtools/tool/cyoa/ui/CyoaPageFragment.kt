package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
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

    private var pageId by arg<String>()

    private val dataModel by activityViewModels<MultiLanguageToolActivityDataModel>()
    private val toolState by activityViewModels<ToolStateHolder>()
    private val pageInsets by activityViewModels<PageInsets>()

    @Inject
    internal lateinit var controllerFactory: ContentPageController.Factory

    // region Lifecycle
    override fun onBindingCreated(binding: CyoaPageContentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.contentInsets = pageInsets.insets
        binding.bindController(controllerFactory, toolState.toolState)
            .also { page.map { it as? ContentPage }.observe(viewLifecycleOwner, it) }
    }
    // endregion Lifecycle

    // region Page model
    private val page by lazy { dataModel.activeManifest.map { it?.findPage(pageId) } }
    // endregion Page model
}
