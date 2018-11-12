package org.cru.godtools.article.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.databinding.ListItemArticleBinding
import org.cru.godtools.xml.model.Manifest

class ArticlesAdapter : SimpleDataBindingAdapter<ListItemArticleBinding>(), Observer<List<Article>> {
    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    var callbacks: Callbacks? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }
    var manifest: Manifest? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }
    var articles: List<Article>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return articles?.size ?: 0
    }

    // region LifeCycle Events

    override fun onChanged(articles: List<Article>?) {
        this.articles = articles
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ListItemArticleBinding {
        return ListItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewDataBinding(binding: ListItemArticleBinding, position: Int) {
        binding.callbacks = callbacks
        binding.article = articles?.get(position)
        binding.manifest = manifest
    }

    override fun onViewDataBindingRecycled(binding: ListItemArticleBinding) {
        binding.article = null
        binding.callbacks = null
        binding.manifest = null
    }

    // endregion LifeCycle Events
}
