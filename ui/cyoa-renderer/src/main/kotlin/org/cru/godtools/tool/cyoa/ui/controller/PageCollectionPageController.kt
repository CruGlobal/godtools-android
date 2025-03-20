package org.cru.godtools.tool.cyoa.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.Insets
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.androidx.viewpager2.adapter.PrimaryItemChangeObserver
import org.ccci.gto.android.common.androidx.viewpager2.adapter.onUpdatePrimaryItem
import org.ccci.gto.android.common.androidx.viewpager2.widget.whileMaintainingVisibleCurrentItem
import org.ccci.gto.android.common.util.Ids
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.tool.parser.model.page.ContentPage
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.shared.tool.parser.model.page.PageCollectionPage
import org.cru.godtools.shared.tool.state.State
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPageContentBinding
import org.cru.godtools.tool.cyoa.databinding.CyoaPagePageCollectionBinding
import org.cru.godtools.tool.cyoa.ui.CyoaPageFragment
import org.cru.godtools.tool.tips.ShowTipCallback
import org.greenrobot.eventbus.EventBus

class PageCollectionPageController @AssistedInject constructor(
    @Assisted
    @get:VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val binding: CyoaPagePageCollectionBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted private val contentInsets: StateFlow<Insets>,
    @Assisted override val enableTips: LiveData<Boolean>,
    @Assisted override val toolState: State,
    eventBus: EventBus,
    private val contentPageControllerFactory: ContentPageController.Factory,
) : BaseController<PageCollectionPage>(PageCollectionPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(
            binding: CyoaPagePageCollectionBinding,
            lifecycleOwner: LifecycleOwner,
            contentInsets: StateFlow<Insets>,
            enableTips: LiveData<Boolean>,
            toolState: State,
        ): PageCollectionPageController
    }

    internal var callbacks: ShowTipCallback? = null

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
        bindPages(model?.pages.orEmpty())
    }

    fun onUpdatePageParams(params: Map<String, String?>) {
        params[CyoaPageFragment.PARAM_POSITION]?.toIntOrNull()
            ?.let { binding.pages.setCurrentItem(it, false) }

        params[PageCollectionPage.PARENT_PARAM_ACTIVE_PAGE]
            ?.let { activePage -> model?.pages?.indexOfFirst { it.id == activePage }?.takeUnless { it == -1 } }
            ?.let { binding.pages.setCurrentItem(it, false) }
    }

    fun onNewPageEvent(event: Event): Boolean {
        binding.pages.currentItem =
            model?.pages?.indexOfFirst { event.id in it.listeners }?.takeIf { it != -1 } ?: return false
        return true
    }

    // region Pages ViewPager
    private val adapter = CyoaPageCollectionPagePageDataBindingAdapter()

    private fun bindPages(pages: List<Page>) {
        binding.pages.whileMaintainingVisibleCurrentItem {
            adapter.pages = pages
            // lazily set the adapter now that pages are bound so it will correctly restore SavedState
            if (binding.pages.adapter == null) binding.pages.adapter = adapter
        }
    }

    inner class CyoaPageCollectionPagePageDataBindingAdapter : SimpleDataBindingAdapter<ViewDataBinding>() {
        init {
            setHasStableIds(true)
        }

        private var primaryItemObserver: PrimaryItemChangeObserver<*>? = null

        var pages = emptyList<Page>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount() = pages.size
        override fun getItemId(position: Int) = Ids.generate(pages[position].id)
        override fun getItemViewType(position: Int) = when (pages[position]) {
            is ContentPage -> R.layout.cyoa_page_content
            else -> R.layout.cyoa_page_content
        }

        // region Lifecycle
        override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding =
            DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false)
                .also {
                    it.lifecycleOwner = this@PageCollectionPageController.lifecycleOwner
                        ?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }

                    when (it) {
                        is CyoaPageContentBinding -> {
                            it.contentInsets = contentInsets
                            it.bindController(contentPageControllerFactory, this@PageCollectionPageController)
                        }
                    }
                }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            primaryItemObserver = onUpdatePrimaryItem(recyclerView) { primary, previous ->
                (previous?.binding?.lifecycleOwner as? ConstrainedStateLifecycleOwner)?.apply {
                    maxState = minOf(maxState, Lifecycle.State.STARTED)
                }
                (primary?.binding?.lifecycleOwner as? ConstrainedStateLifecycleOwner)?.apply {
                    maxState = maxOf(maxState, Lifecycle.State.RESUMED)
                }
            }
        }

        override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
            when (binding) {
                is CyoaPageContentBinding -> binding.controller?.model = pages[position] as? ContentPage
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            primaryItemObserver?.unregister()
            primaryItemObserver = null
        }
        // endregion Lifecycle
    }
    // endregion Pages ViewPager
}

fun CyoaPagePageCollectionBinding.bindController(
    factory: PageCollectionPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    contentInsets: StateFlow<Insets>,
    enableTips: LiveData<Boolean>,
    toolState: State,
) = controller ?: factory.create(this, lifecycleOwner, contentInsets, enableTips, toolState)
