package org.cru.godtools.article.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.recyclerview.decorator.VerticalSpaceItemDecoration
import org.cru.godtools.article.R
import org.cru.godtools.article.databinding.ArticleCategoriesFragmentBinding
import org.cru.godtools.base.tool.analytics.model.SCREEN_CATEGORIES
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.tool.model.Category
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class CategoriesFragment : BaseToolFragment<ArticleCategoriesFragmentBinding>, CategorySelectedListener {
    constructor() : super(R.layout.article_categories_fragment)
    constructor(code: String, locale: Locale) : super(R.layout.article_categories_fragment, code, locale)

    @Inject
    internal lateinit var eventBus: EventBus

    // region Lifecycle
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ArticleCategoriesFragmentBinding.inflate(inflater, container, false).apply { setupCategoriesView() }

    override fun onResume() {
        super.onResume()
        eventBus.post(ToolAnalyticsScreenEvent(SCREEN_CATEGORIES, tool, locale))
    }

    override fun onCategorySelected(category: Category?) {
        findListener<CategorySelectedListener>()?.onCategorySelected(category)
    }
    // endregion Lifecycle

    // region Categories View
    private fun ArticleCategoriesFragmentBinding.setupCategoriesView() {
        categories.apply {
            setHasFixedSize(true)
            addItemDecoration(VerticalSpaceItemDecoration(R.dimen.categories_list_gap))
            adapter = CategoriesAdapter(viewLifecycleOwner).apply {
                callbacks.set(this@CategoriesFragment)
                toolDataModel.manifest.map { it?.categories }.observe(viewLifecycleOwner, this)
            }
        }
    }
    // endregion Categories View
}
