package org.cru.godtools.article.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import okio.FileSystem
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.analytics.model.SCREEN_CATEGORIES
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.renderer.article.RenderArticleCategory
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.Category
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg

@AndroidEntryPoint
class CategoriesFragment() : Fragment() {
    constructor(code: String, locale: Locale) : this() {
        tool = code
        this.locale = locale
    }

    private var tool by arg<String>()
    private var locale by arg<Locale>()

    @Inject
    internal lateinit var eventBus: EventBus

    @Inject
    @Named(TOOL_RESOURCE_FILE_SYSTEM)
    internal lateinit var resourceFileSystem: FileSystem

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
                    ProvideRendererServices(resourceFileSystem) {
                        CategoriesContent()
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        eventBus.post(ToolAnalyticsScreenEvent(SCREEN_CATEGORIES, tool, locale))
    }

    private fun onCategorySelected(category: Category?) {
        findListener<CategorySelectedListener>()?.onCategorySelected(category)
    }
    // endregion Lifecycle

    // region Data Model
    private val toolDataModel: LatestPublishedManifestDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.toolCode.value = tool
        toolDataModel.locale.value = locale
    }
    // endregion Data Model

    @Composable
    private fun CategoriesContent(modifier: Modifier = Modifier) {
        val manifest by toolDataModel.manifestFlow.collectAsState(null)
        val categories by remember { derivedStateOf { manifest?.categories.orEmpty() } }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = modifier.fillMaxSize(),
        ) {
            items(categories, key = { it.id ?: it }) { category ->
                RenderArticleCategory(
                    category,
                    modifier = Modifier.clickable { onCategorySelected(category) }
                )
            }
        }
    }
}
