package org.cru.godtools.article.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.databinding.ListItemArticleBinding
import org.cru.godtools.xml.model.Manifest

class ArticlesAdapter(
    lifecycleOwner: LifecycleOwner?,
    private val manifest: LiveData<Manifest?>
) : SimpleDataBindingAdapter<ListItemArticleBinding>(lifecycleOwner), Observer<List<Article>?> {
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
    override fun getItemId(position: Int) = getItem(position)?.uri?.let { IdUtils.convertId(it) } ?: NO_ID

    // region Lifecycle
    override fun onChanged(articles: List<Article>?) {
        this.articles = articles
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
