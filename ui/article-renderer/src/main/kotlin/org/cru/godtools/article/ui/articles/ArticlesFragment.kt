package org.cru.godtools.article.ui.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import com.github.ajalt.colormath.extensions.android.composecolor.toComposeColor
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTracker
import org.cru.godtools.article.aem.db.ArticleDao
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.analytics.model.ArticlesAnalyticsScreenEvent
import org.cru.godtools.article.analytics.model.ArticlesCategoryAnalyticsScreenEvent
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.backgroundColor
import org.cru.godtools.shared.tool.parser.model.primaryColor
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull

@AndroidEntryPoint
class ArticlesFragment() : Fragment() {
    constructor(code: String, locale: Locale, category: String? = null) : this() {
        tool = code
        this.locale = locale
        this.category = category
    }

    interface Callbacks {
        fun onArticleSelected(article: Article?)
    }

    private var tool by arg<String>()
    private var locale by arg<Locale>()
    internal var category by argOrNull<String>()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GodToolsTheme {
                    ArticlesContent()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        sendAnalyticsEvent()
    }
    // endregion Lifecycle

    // region Data Model
    private val toolDataModel: ArticlesFragmentDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.toolCode.value = tool
        toolDataModel.locale.value = locale
        toolDataModel.category.value = category
    }
    // endregion Data Model

    @Inject
    internal lateinit var aemArticleManager: AemArticleManager

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ArticlesContent(modifier: Modifier = Modifier) {
        val manifest by toolDataModel.manifestFlow.collectAsState(null)
        val articles by toolDataModel.articles.collectAsState(emptyList())

        val syncTracker = rememberSyncTracker()
        val isSyncing by syncTracker.isSyncing.collectAsState()
        LaunchedEffect(manifest) { syncTracker.syncData(manifest) }

        val primaryColor = remember(manifest) { manifest.primaryColor.toComposeColor() }
        val backgroundColor = remember(manifest) { manifest.backgroundColor.toComposeColor() }

        CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(primaryColor)) {
            PullToRefreshBox(
                isRefreshing = isSyncing,
                onRefresh = { syncTracker.syncData(manifest, true) },
                modifier = modifier
                    .fillMaxSize()
                    .background(backgroundColor),
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(articles, key = { it.uri }) { article ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    article.title,
                                    maxLines = 1,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                headlineColor = primaryColor,
                                containerColor = backgroundColor,
                            ),
                            modifier = Modifier
                                .clickable { findListener<Callbacks>()?.onArticleSelected(article) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    private fun SyncTracker.syncData(manifest: Manifest?, force: Boolean = false) = launchSync {
        aemArticleManager.syncAemImportsFromManifest(manifest, force)
    }

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
internal class ArticlesFragmentDataModel @Inject constructor(
    manifestManager: ManifestManager,
    private val articleDao: ArticleDao
) : LatestPublishedManifestDataModel(manifestManager) {
    internal val category = MutableStateFlow<String?>(null)

    private val tags = manifestFlow.combine(category) { manifest, category ->
        when (category) {
            null -> null
            else -> manifest?.findCategory(category)?.aemTags.orEmpty()
        }
    }

    internal val articles = combineTransformLatest(toolCode.asFlow(), locale.asFlow(), tags) { tool, locale, tags ->
        when {
            tool == null || locale == null -> emit(emptyList())
            tags == null -> emitAll(articleDao.getArticlesFlow(tool, locale))
            else -> emitAll(articleDao.getArticlesFlow(tool, locale, tags.toList()))
        }
    }
}
