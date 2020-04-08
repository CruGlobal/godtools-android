package org.cru.godtools.article.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import org.ccci.gto.android.common.recyclerview.decorator.VerticalSpaceItemDecoration
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.cru.godtools.article.R
import org.cru.godtools.article.R2
import org.cru.godtools.article.adapter.CategoriesAdapter
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.xml.model.Category
import java.util.Locale

class CategoriesFragment : BaseToolFragment<ViewDataBinding>, CategoriesAdapter.Callbacks {
    constructor() : super(R.layout.fragment_categories)
    constructor(code: String, locale: Locale) : super(R.layout.fragment_categories, code, locale)

    interface Callbacks {
        fun onCategorySelected(category: Category?)
    }

    override val hasDataBinding get() = false

    @JvmField
    @BindView(R2.id.categories)
    internal var categoriesView: RecyclerView? = null
    private var categoriesAdapter: CategoriesAdapter? = null

    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoriesView()
    }

    @CallSuper
    override fun onManifestUpdated() {
        super.onManifestUpdated()
        updateCategoriesView()
    }

    override fun onCategorySelected(category: Category?) {
        FragmentUtils.getListener(this, Callbacks::class.java)
            ?.onCategorySelected(category)
    }

    override fun onDestroyView() {
        cleanupCategoriesView()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region Categories View

    private fun setupCategoriesView() {
        categoriesView?.apply {
            setHasFixedSize(true)
            addItemDecoration(VerticalSpaceItemDecoration(R.dimen.categories_list_gap))

            categoriesAdapter = CategoriesAdapter()
                .apply { setCallbacks(this@CategoriesFragment) }
            adapter = categoriesAdapter
            updateCategoriesView()
        }
    }

    private fun updateCategoriesView() {
        categoriesAdapter?.categories = manifest?.categories
    }

    private fun cleanupCategoriesView() {
        categoriesAdapter?.setCallbacks(null)
        categoriesAdapter = null
    }

    // endregion Categories View
}
