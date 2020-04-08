package org.cru.godtools.article.ui.categories

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.map
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import org.ccci.gto.android.common.recyclerview.decorator.VerticalSpaceItemDecoration
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.cru.godtools.article.R
import org.cru.godtools.article.R2
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.xml.model.Category
import java.util.Locale

class CategoriesFragment : BaseToolFragment<ViewDataBinding>, CategoriesAdapter.Callbacks {
    constructor() : super(R.layout.article_categories_fragment)
    constructor(code: String, locale: Locale) : super(R.layout.article_categories_fragment, code, locale)

    interface Callbacks {
        fun onCategorySelected(category: Category?)
    }

    override val hasDataBinding get() = false

    @JvmField
    @BindView(R2.id.categories)
    internal var categoriesView: RecyclerView? = null

    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoriesView()
    }

    override fun onCategorySelected(category: Category?) {
        FragmentUtils.getListener(this, Callbacks::class.java)
            ?.onCategorySelected(category)
    }
    // endregion Lifecycle

    // region Categories View
    private val categoriesAdapter by lazy {
        CategoriesAdapter(this).also {
            it.setCallbacks(this)
            toolDataModel.manifest.map { it?.categories }.observe(this, it)
        }
    }

    private fun setupCategoriesView() {
        categoriesView?.apply {
            setHasFixedSize(true)
            addItemDecoration(VerticalSpaceItemDecoration(R.dimen.categories_list_gap))
            adapter = categoriesAdapter
        }
    }
    // endregion Categories View
}
