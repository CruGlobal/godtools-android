package org.cru.godtools.article.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
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
import splitties.fragmentargs.argOrNull
import java.util.Locale

internal val resetRefreshLayoutTask = WeakTask.Task<SwipeRefreshLayout> { it.isRefreshing = false }

class ArticlesFragment() : BaseToolFragment(), ArticlesAdapter.Callbacks, SwipeRefreshLayout.OnRefreshListener {
    constructor(code: String, locale: Locale, category: String? = null) : this() {
        arguments = Bundle(3).apply { populateArgs(this, code, locale) }
        this.category = category
    }

    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private var category by argOrNull<String>()

    private var binding: FragmentArticlesBinding? = null
    @JvmField
    @BindView(R2.id.article_swipe_container)
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
        updateDataModelTags()
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
        updateDataModelTags()
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
        cleanupDataBinding()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region Data Model
    private val dataModel: ArticlesFragmentDataModel by viewModels()

    private fun setupDataModel() {
        dataModel.tool.value = mTool
        dataModel.locale.value = mLocale
    }

    private fun updateDataModelTags() {
        dataModel.tags.value = when {
            // lookup AEM tags from the manifest category
            category != null -> mManifest?.findCategory(category)?.orElse(null)?.aemTags.orEmpty()
            // no category, so show all articles for this tool
            else -> null
        }
    }
    // endregion Data Model

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
        binding = DataBindingUtil.bind(view)
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
    @JvmField
    @BindView(R2.id.articles)
    internal var articlesView: RecyclerView? = null

    private val articlesAdapter: ArticlesAdapter by lazy {
        ArticlesAdapter()
            .apply { setCallbacks(this@ArticlesFragment) }
            .also { dataModel.articles.observe(this, it) }
    }

    private fun setupArticlesView() {
        articlesView?.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = articlesAdapter
        }
        updateArticlesViewManifest()
    }

    private fun updateArticlesViewManifest() = articlesAdapter.setManifest(mManifest)
    // endregion ArticlesView

    private fun setupSwipeRefresh() = swipeRefreshLayout?.setOnRefreshListener(this)
    // endregion View Logic
}

class ArticlesFragmentDataModel(application: Application) : AndroidViewModel(application) {
    private val aemDb = ArticleRoomDatabase.getInstance(application)

    internal var tool = MutableLiveData<String>()
    internal var locale = MutableLiveData<Locale>()
    internal var tags = MutableLiveData<Set<String>?>(null)

    internal val articles = tool.switchCombineWith(locale, tags) { tool, locale, tags ->
        when {
            tool == null || locale == null -> emptyLiveData<List<Article>>()
            tags == null -> aemDb.articleDao().getArticles(tool, locale)
            else -> aemDb.articleDao().getArticles(tool, locale, tags.toList())
        }
    }
}
