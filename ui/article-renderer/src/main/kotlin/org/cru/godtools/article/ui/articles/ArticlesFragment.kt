package org.cru.godtools.article.ui.articles

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.article.aem.db.ArticleDao
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.analytics.model.ArticlesAnalyticsScreenEvent
import org.cru.godtools.article.analytics.model.ArticlesCategoryAnalyticsScreenEvent
import org.cru.godtools.base.tool.fragment.BaseToolFragment
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.tool.article.R
import org.cru.godtools.tool.article.databinding.FragmentArticlesBinding
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.argOrNull

@AndroidEntryPoint
class ArticlesFragment :
    BaseToolFragment<FragmentArticlesBinding>,
    ArticlesAdapter.Callbacks {
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

    internal var category by argOrNull<String>()

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

    override fun onResume() {
        super.onResume()
        sendAnalyticsEvent()
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
            aemArticleManager.syncAemImportsFromManifest(toolDataModel.manifest.value, force)
            isSyncing.value = false
        }
    }

    // region View Logic
    // region ArticlesView
    private fun FragmentArticlesBinding.setupArticlesView() {
        articles.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = ArticlesAdapter(viewLifecycleOwner, toolDataModel.manifest).apply {
                callbacks.set(this@ArticlesFragment)
                toolDataModel.articles.observe(viewLifecycleOwner, this)
            }
        }
    }
    // endregion ArticlesView

    private fun FragmentArticlesBinding.setupSwipeRefresh() {
        refresh.setOnRefreshListener { syncData(true) }
        isSyncing.observe(viewLifecycleOwner) { refresh.isRefreshing = it }
    }
    // endregion View Logic

    @Inject
    internal lateinit var eventBus: EventBus

    private fun sendAnalyticsEvent() {
        when {
            category != null -> category?.let { ArticlesCategoryAnalyticsScreenEvent(tool, locale, it) }
            else -> ArticlesAnalyticsScreenEvent(tool, locale)
        }?.let { eventBus.post(it) }
    }
}

@HiltViewModel
class ArticlesFragmentDataModel @Inject constructor(
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

    internal val articles = toolCode.switchCombineWith(locale, tags) { tool, locale, tags ->
        when {
            tool == null || locale == null -> emptyLiveData<List<Article>>()
            tags == null -> articleDao.getArticles(tool, locale)
            else -> articleDao.getArticles(tool, locale, tags.toList())
        }
    }
}
