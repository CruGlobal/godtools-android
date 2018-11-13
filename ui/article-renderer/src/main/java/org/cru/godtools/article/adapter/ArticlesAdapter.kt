package org.cru.godtools.article.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.databinding.ListItemArticleBinding
import org.cru.godtools.xml.model.Manifest

class ArticlesAdapter : SimpleDataBindingAdapter<ListItemArticleBinding>(), Observer<List<Article>> {
    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private val callbacks = ObservableField<Callbacks>()
    private val manifest = ObservableField<Manifest>()
    var articles: List<Article>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setCallbacks(callbacks: Callbacks?) = this.callbacks.set(callbacks)

    fun setManifest(manifest: Manifest?) = this.manifest.set(manifest)

    override fun getItemCount(): Int {
        return articles?.size ?: 0
    }

    // region LifeCycle Events

    override fun onChanged(articles: List<Article>?) {
        this.articles = articles
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ListItemArticleBinding {
        return ListItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also { it.callbacks = callbacks }
            .also { it.manifest = manifest }
    }

    override fun onBindViewDataBinding(binding: ListItemArticleBinding, position: Int) {
        binding.article = articles?.get(position)
    }

    override fun onViewDataBindingRecycled(binding: ListItemArticleBinding) {
        binding.article = null
    }

    // endregion LifeCycle Events
}
