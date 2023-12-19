package org.cru.godtools.ui.dashboard.tools

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import org.cru.godtools.base.Settings
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen

class ToolsPresenter @AssistedInject constructor(
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<ToolsScreen.State> {
    @Composable
    override fun present(): ToolsScreen.State {
        val viewModel: ToolsViewModel = viewModel()

        val selectedLanguage by viewModel.selectedLanguage.collectAsState()

        val eventSink: (ToolsScreen.Event) -> Unit = remember {
            {
                when (it) {
                    is ToolsScreen.Event.OpenToolDetails -> {
                        if (it.source != null) viewModel.recordOpenToolDetailsInAnalytics(it.tool, it.source)
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLanguage?.code))
                    }
                    is ToolsScreen.Event.UpdateSelectedCategory -> viewModel.setSelectedCategory(it.category)
                    is ToolsScreen.Event.UpdateLanguageQuery -> viewModel.setLanguageQuery(it.query)
                    is ToolsScreen.Event.UpdateSelectedLanguage -> viewModel.setSelectedLocale(it.locale)
                }
            }
        }

        return ToolsScreen.State(
            banner = rememberBanner(),
            spotlightTools = viewModel.spotlightTools.collectAsState().value,
            filters = ToolsScreen.State.Filters(
                categories = viewModel.categories.collectAsState().value,
                selectedCategory = viewModel.selectedCategory.collectAsState().value,
                languages = viewModel.languages.collectAsState().value,
                languageQuery = viewModel.languageQuery.collectAsState().value,
                selectedLanguage = viewModel.selectedLanguage.collectAsState().value,
            ),
            tools = viewModel.tools.collectAsState().value,
            eventSink = eventSink,
        )
    }

    @Composable
    @VisibleForTesting
    internal fun rememberBanner() = remember {
        settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
            .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
    }.collectAsState(null).value

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): ToolsPresenter
    }
}
