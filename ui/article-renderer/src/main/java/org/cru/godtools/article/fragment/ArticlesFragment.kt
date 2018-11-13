package org.cru.godtools.article.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.ccci.gto.android.common.util.MainThreadExecutor
import org.cru.godtools.article.EXTRA_CATEGORY
import org.cru.godtools.article.R
import org.cru.godtools.article.R2
import org.cru.godtools.article.adapter.ArticlesAdapter
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.article.databinding.FragmentArticlesBinding
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.base.util.WeakTask
import java.util.Locale

fun newArticlesFragment(code: String, locale: Locale, category: String? = null): ArticlesFragment {
    val args = Bundle(3).apply {
        BaseToolFragment.populateArgs(this, code, locale)
        putString(EXTRA_CATEGORY, category)
    }
    return ArticlesFragment().apply {
        arguments = args
    }
}

private val resetRefreshLayoutTask = WeakTask.Task<SwipeRefreshLayout> { it.isRefreshing = false }

class ArticlesFragment : BaseToolFragment(), ArticlesAdapter.Callbacks, SwipeRefreshLayout.OnRefreshListener {
    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private val category: String? by lazy { arguments?.getString(EXTRA_CATEGORY, null) }
    private val viewModel: ArticleListViewModel by lazy {
        ViewModelProviders.of(this).get(ArticleListViewModel::class.java)
    }

    private var binding: FragmentArticlesBinding? = null
    @JvmField
    @BindView(R2.id.articles)
    internal var articlesView: RecyclerView? = null
    @JvmField
    @BindView(R2.id.article_swipe_container)
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var articlesAdapter: ArticlesAdapter? = null

    private lateinit var articles: LiveData<List<Article>>

    // region LifeCycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateViewModelArticles()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataBinding(view)
        setupArticlesView()
        setupSwipeRefresh()
    }

    override fun onManifestUpdated() {
        super.onManifestUpdated()
        updateDataBindingManifest()
        updateArticlesViewManifest()
        updateViewModelArticles()
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

    override fun onDestroyView() {
        cleanupArticlesView()
        cleanupDataBinding()
        super.onDestroyView()
    }

    // endregion LifeCycle Events

    // region ViewModel methods

    private fun updateViewModelArticles() {
        val old = if (this::articles.isInitialized) articles else null
        articles = when {
            // lookup AEM tags from the manifest category
            category != null -> {
                val tags = mManifest?.findCategory(category)?.orElse(null)?.aemTags ?: setOf()
                viewModel.getArticlesForTags(mTool, mLocale, tags)
            }
            // no category, so show all articles for this tool
            else -> viewModel.getArticles(mTool, mLocale)
        }

        if (articles !== old) {
            old?.removeObservers(this)
            updateArticlesViewArticles()
        }
    }

    // endregion ViewModel methods

    private fun syncData(force: Boolean) {
        AemArticleManger.getInstance(requireContext())
            .enqueueSyncManifestAemImports(mManifest, force)
            .apply {
                swipeRefreshLayout?.let {
                    addListener(WeakTask(it, resetRefreshLayoutTask), MainThreadExecutor())
                }
            }
    }

    // region View Logic

    // region Data Binding

    private fun setupDataBinding(view: View) {
        binding = FragmentArticlesBinding.bind(view)
        updateDataBindingManifest()
    }

    private fun updateDataBindingManifest() {
        binding?.manifest = mManifest
    }

    private fun cleanupDataBinding() {
        binding = null
    }

    // endregion Data Binding

    // region ArticlesView

    private fun setupArticlesView() {
        articlesView
            ?.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = ArticlesAdapter()
                    .apply { setCallbacks(this@ArticlesFragment) }
                    .also { articlesAdapter = it }
            }
            ?.also {
                updateArticlesViewManifest()
                updateArticlesViewArticles()
            }
    }

    private fun updateArticlesViewManifest() = articlesAdapter?.setManifest(mManifest)

    private fun updateArticlesViewArticles() = articlesAdapter?.let { articles.observe(this, it) }

    private fun cleanupArticlesView() {
        articlesAdapter?.apply {
            setCallbacks(null)
            this@ArticlesFragment.articles.removeObserver(this)
        }
        articlesAdapter = null
    }

    // endregion ArticlesView

    private fun setupSwipeRefresh() = swipeRefreshLayout?.setOnRefreshListener(this)

    // endregion View Logic

    class ArticleListViewModel(application: Application) : AndroidViewModel(application) {
        private val aemDb = ArticleRoomDatabase.getInstance(application)

        private var tool: String? = null
        private var locale: Locale? = null
        private var tags: Set<String>? = null
        private lateinit var articles: LiveData<List<Article>>

        internal fun getArticles(tool: String, locale: Locale): LiveData<List<Article>> {
            if (isArticlesLiveDataStale(tool, locale, null)) {
                this.tool = tool
                this.locale = locale
                tags = null
                articles = aemDb.articleDao().getArticles(tool, locale)
            }

            return articles
        }

        internal fun getArticlesForTags(tool: String, locale: Locale, tags: Set<String>): LiveData<List<Article>> {
            if (isArticlesLiveDataStale(tool, locale, tags)) {
                this.tool = tool
                this.locale = locale
                this.tags = tags
                articles = aemDb.articleDao().getArticles(tool, locale, tags.toList())
            }

            return articles
        }

        private fun isArticlesLiveDataStale(tool: String, locale: Locale, tags: Set<String>?): Boolean {
            return !this::articles.isInitialized || this.tool != tool || this.locale != locale || this.tags != tags
        }
    }
}
