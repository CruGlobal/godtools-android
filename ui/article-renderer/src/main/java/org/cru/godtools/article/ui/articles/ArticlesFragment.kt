package org.cru.godtools.article.ui.articles

import android.os.Bundle
import androidx.concurrent.futures.await
import androidx.fragment.app.viewModels
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.article.R
import org.cru.godtools.article.aem.db.ArticleDao
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.databinding.FragmentArticlesBinding
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import splitties.fragmentargs.argOrNull

@AndroidEntryPoint
class ArticlesFragment : BaseToolFragment<FragmentArticlesBinding>, ArticlesAdapter.Callbacks {
    constructor() : super(R.layout.fragment_articles)
    constructor(
        code: String,
        locale: Locale,
        category: String? = null
    ) : super(R.layout.fragment_articles, code, locale) {
        this.category = category
    }

    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private var category by argOrNull<String>()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onBindingCreated(binding: FragmentArticlesBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.manifest = toolDataModel.manifest
        binding.setupArticlesView()
        binding.setupSwipeRefresh()
    }

    /**
     * This is the callback method from ArticleAdapter that will handle the functionality of an article being selected
     * from the list.
     *
     * @param article the selected Article
     */
    override fun onArticleSelected(article: Article?) {
        findListener<Callbacks>()?.onArticleSelected(article)
    }
    // endregion Lifecycle

    // region Data Model
    override val toolDataModel: ArticlesFragmentDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.category.value = category
    }
    // endregion Data Model

    @Inject
    internal lateinit var aemArticleManager: AemArticleManager

    private val isSyncing = MutableLiveData(false)
    private fun syncData(force: Boolean = false) {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            isSyncing.value = true
            aemArticleManager.enqueueSyncManifestAemImports(toolDataModel.manifest.value, force).await()
            isSyncing.value = false
        }
    }

    // region View Logic
    // region ArticlesView
    private val articlesAdapter by lazy {
        ArticlesAdapter(this, toolDataModel.manifest).also {
            it.callbacks.set(this)
            toolDataModel.articles.observe(this, it)
        }
    }

    private fun FragmentArticlesBinding.setupArticlesView() {
        articles.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = articlesAdapter
        }
    }
    // endregion ArticlesView

    private fun FragmentArticlesBinding.setupSwipeRefresh() {
        refresh.setOnRefreshListener { syncData(true) }
        isSyncing.observe(viewLifecycleOwner) { refresh.isRefreshing = it }
    }
    // endregion View Logic
}

class ArticlesFragmentDataModel @ViewModelInject constructor(
    manifestManager: ManifestManager,
    private val articleDao: ArticleDao
) : LatestPublishedManifestDataModel(manifestManager) {
    internal val category = MutableLiveData<String?>()

    private val tags = manifest.combineWith(category) { manifest, category ->
        when (category) {
            null -> null
            else -> manifest?.findCategory(category)?.aemTags.orEmpty()
        }
    }

    internal val articles =
        toolCode.switchCombineWith(locale, tags) { tool, locale, tags ->
        when {
            tool == null || locale == null -> emptyLiveData<List<Article>>()
            tags == null -> articleDao.getArticles(tool, locale)
            else -> articleDao.getArticles(tool, locale, tags.toList())
        }
    }
}
