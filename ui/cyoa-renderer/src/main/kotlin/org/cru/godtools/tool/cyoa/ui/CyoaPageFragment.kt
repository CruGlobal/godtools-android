package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.tool.cyoa.BR
import org.cru.godtools.tool.tips.ShowTipCallback
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg

abstract class CyoaPageFragment<B : ViewDataBinding, C : BaseController<*>>(@LayoutRes layoutId: Int, page: String?) :
    BaseFragment<B>(layoutId), ShowTipCallback {
    @Inject
    protected lateinit var eventBus: EventBus

    protected val dataModel by activityViewModels<MultiLanguageToolActivityDataModel>()
    internal val toolState by activityViewModels<ToolStateHolder>()
    private val pageInsets by activityViewModels<PageInsets>()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page.observe(this) { triggerInvalidPageListenerIfNeeded(it) }
    }

    override fun onBindingCreated(binding: B, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.setVariable(BR.contentInsets, pageInsets.insets)
        setupPageController(binding)
    }

    override fun onResume() {
        super.onResume()
        page.notNull().observeOnce(this) { triggerAnalyticsScreenView() }
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
    internal val page by lazy { dataModel.manifest.map { it?.findPage(pageId) }.asLiveData() }

    init {
        page?.let { pageId = page }
    }

    // region InvalidPageListener
    fun interface InvalidPageListener {
        fun onInvalidPage(fragment: CyoaPageFragment<*, *>, page: Page?)
    }

    internal abstract fun supportsPage(page: Page): Boolean

    private fun triggerInvalidPageListenerIfNeeded(page: Page?) {
        if (page == null || !supportsPage(page)) findListener<InvalidPageListener>()?.onInvalidPage(this, page)
    }
    // endregion InvalidPageListener

    // region Controller
    protected var controller: C? = null

    protected open fun setupPageController(binding: B) = Unit
    protected open fun cleanupPageController() {
        controller = null
    }
    // endregion Controller
    // endregion Page

    // region Training Tips
    override fun showTip(tip: Tip) {
        findListener<ShowTipCallback>()?.showTip(tip)
    }
    // endregion Training Tips

    // region Analytics
    protected abstract fun triggerAnalyticsScreenView()
    // endregion Analytics
}
