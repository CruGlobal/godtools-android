package org.cru.godtools.article.ui.articles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.util.Ids
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.tool.article.databinding.ListItemArticleBinding

class ArticlesAdapter(lifecycleOwner: LifecycleOwner?, private val manifest: LiveData<Manifest?>) :
    SimpleDataBindingAdapter<ListItemArticleBinding>(lifecycleOwner),
    Observer<List<Article>?> {
    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    init {
        setHasStableIds(true)
    }

    val callbacks = ObservableField<Callbacks>()
    private var articles: List<Article>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = articles?.size ?: 0
    private fun getItem(position: Int) = articles?.get(position)
    override fun getItemId(position: Int) = getItem(position)?.uri?.let { Ids.generate(it) } ?: NO_ID

    // region Lifecycle
    override fun onChanged(value: List<Article>?) {
        this.articles = value
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        ListItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.callbacks = callbacks
            it.manifest = manifest
        }

    override fun onBindViewDataBinding(binding: ListItemArticleBinding, position: Int) {
        binding.article = getItem(position)
    }

    override fun onViewDataBindingRecycled(binding: ListItemArticleBinding) {
        binding.article = null
    }
    // endregion Lifecycle
}
