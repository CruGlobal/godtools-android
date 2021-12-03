package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.tool.cyoa.BR
import splitties.fragmentargs.arg

abstract class CyoaPageFragment<B : ViewDataBinding>(@LayoutRes layoutId: Int, page: String?) :
    BaseFragment<B>(layoutId) {
    private val dataModel by activityViewModels<MultiLanguageToolActivityDataModel>()
    internal val toolState by activityViewModels<ToolStateHolder>()
    private val pageInsets by activityViewModels<PageInsets>()

    // region Lifecycle
    override fun onBindingCreated(binding: B, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.setVariable(BR.contentInsets, pageInsets.insets)
        setupPageController(binding)
    }

    internal fun onContentEvent(event: Event) {
        controller?.onContentEvent(event)
    }

    override fun onDestroyBinding(binding: B) {
        cleanupPageController()
    }
    // endregion Lifecycle

    // region Page
    internal var pageId by arg<String>()
        private set
    internal val page by lazy { dataModel.activeManifest.map { it?.findPage(pageId) } }

    init {
        page?.let { pageId = page }
    }

    // region Controller
    protected var controller: BaseController<*>? = null

    protected open fun setupPageController(binding: B) = Unit
    protected open fun cleanupPageController() {
        controller = null
    }
    // endregion Controller
    // endregion Page
}
