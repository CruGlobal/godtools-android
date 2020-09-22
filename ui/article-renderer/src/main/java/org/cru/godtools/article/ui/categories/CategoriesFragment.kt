package org.cru.godtools.article.ui.categories

import android.os.Bundle
import android.view.View
import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.recyclerview.decorator.VerticalSpaceItemDecoration
import org.cru.godtools.article.R
import org.cru.godtools.article.databinding.ArticleCategoriesFragmentBinding
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.xml.model.Category

@AndroidEntryPoint
class CategoriesFragment : BaseToolFragment<ArticleCategoriesFragmentBinding>, CategorySelectedListener {
    constructor() : super(R.layout.article_categories_fragment)
    constructor(code: String, locale: Locale) : super(R.layout.article_categories_fragment, code, locale)

    override val View.viewBinding get() = ArticleCategoriesFragmentBinding.bind(this)

    // region Lifecycle
    override fun onBindingCreated(binding: ArticleCategoriesFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.setupCategoriesView()
    }

    override fun onCategorySelected(category: Category?) {
        findListener<CategorySelectedListener>()?.onCategorySelected(category)
    }
    // endregion Lifecycle

    // region Categories View
    private val categoriesAdapter by lazy {
        CategoriesAdapter(this).also {
            it.callbacks.set(this)
            toolDataModel.manifest.map { it?.categories }.observe(this, it)
        }
    }

    private fun ArticleCategoriesFragmentBinding.setupCategoriesView() {
        categories.apply {
            setHasFixedSize(true)
            addItemDecoration(VerticalSpaceItemDecoration(R.dimen.categories_list_gap))
            adapter = categoriesAdapter
        }
    }
    // endregion Categories View
}
