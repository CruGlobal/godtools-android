package org.cru.godtools.article.fragment

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import org.ccci.gto.android.common.lifecycle.combineWith
import org.ccci.gto.android.common.lifecycle.emptyLiveData
import org.ccci.gto.android.common.lifecycle.switchCombineWith
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.ccci.gto.android.common.util.MainThreadExecutor
import org.ccci.gto.android.common.util.WeakTask
import org.cru.godtools.article.R
import org.cru.godtools.article.R2
import org.cru.godtools.article.adapter.ArticlesAdapter
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.article.databinding.FragmentArticlesBinding
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import splitties.fragmentargs.argOrNull
import java.util.Locale

internal val resetRefreshLayoutTask = WeakTask.Task<SwipeRefreshLayout> { it.isRefreshing = false }

class ArticlesFragment : BaseToolFragment<FragmentArticlesBinding>, ArticlesAdapter.Callbacks,
    SwipeRefreshLayout.OnRefreshListener {
    constructor() : super(R.layout.fragment_articles)
    constructor(code: String, locale: Locale, category: String? = null) :
        super(R.layout.fragment_articles, code, locale) {
        this.category = category
    }

    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private var category by argOrNull<String>()

    @JvmField
    @BindView(R2.id.article_swipe_container)
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeRefresh()
    }

    override fun onBindingCreated(binding: FragmentArticlesBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.manifest = toolDataModel.manifest
        binding.setupArticlesView()
    }

    override fun onRefresh() = syncData(true)

    /**
     * This is the callback method from ArticleAdapter that will handle the functionality of an article being selected
     * from the list.
     *
     * @param article the selected Article
     */
    override fun onArticleSelected(article: Article?) {
        FragmentUtils.getListener(this, Callbacks::class.java)?.onArticleSelected(article)
    }
    // endregion Lifecycle

    // region Data Model
    override val toolDataModel: ArticlesFragmentDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.category.value = category
    }
    // endregion Data Model

    private fun syncData(force: Boolean) {
        AemArticleManger.getInstance(requireContext())
            .enqueueSyncManifestAemImports(manifest, force)
            .apply {
                swipeRefreshLayout?.let {
                    addListener(WeakTask(it, resetRefreshLayoutTask), MainThreadExecutor())
                }
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

    private fun setupSwipeRefresh() = swipeRefreshLayout?.setOnRefreshListener(this)
    // endregion View Logic
}

class ArticlesFragmentDataModel(application: Application) : LatestPublishedManifestDataModel(application) {
    private val aemDb = ArticleRoomDatabase.getInstance(application)

    internal val category = MutableLiveData<String?>()

    private val tags = manifest.combineWith(category) { manifest, category ->
        when (category) {
            null -> null
            else -> manifest?.findCategory(category)?.orElse(null)?.aemTags.orEmpty()
        }
    }

    internal val articles =
        toolCode.switchCombineWith(locale, tags) { tool, locale, tags ->
        when {
            tool == null || locale == null -> emptyLiveData<List<Article>>()
            tags == null -> aemDb.articleDao().getArticles(tool, locale)
            else -> aemDb.articleDao().getArticles(tool, locale, tags.toList())
        }
    }
}
