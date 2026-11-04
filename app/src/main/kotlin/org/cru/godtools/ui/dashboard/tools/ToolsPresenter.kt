package org.cru.godtools.ui.dashboard.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.coroutines.flow.first
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTask
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.dashboard.personalization.LocalDashboardPersonalizationState
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode
import org.cru.godtools.ui.settings.country.CountrySettingsScreen
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.greenrobot.eventbus.EventBus

class ToolsPresenter @AssistedInject internal constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
    private val toolCardPresenter: ToolCardPresenter,
    private val favoriteToolsBannerPresenter: BannerPresenter<FavoriteToolsBannerPresenter.UiState>,
    private val featuredToolsFlowProducer: FeaturedToolsFlowProducer,
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer,
    private val toolFiltersStateProducer: ToolFiltersStateProducer,
    private val syncService: GodToolsSyncService,
    @Assisted private val circuitContext: CircuitContext,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    data class UiState(
        val mode: Mode = Mode.ALL_TOOLS,
        val banner: Banner.UiState? = null,
        val dataLoaded: Boolean = true,
        val spotlightTools: List<ToolCardPresenter.UiState> = emptyList(),
        val filters: Filters = Filters(),
        val tools: List<ToolCardPresenter.UiState> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState {
        enum class Mode { PERSONALIZATION, ALL_TOOLS }
    }

    sealed interface UiEvent : CircuitUiEvent {
        data class ChangeMode(val mode: Mode) : UiEvent
        data object OpenLocalizationSettings : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val personalization = LocalDashboardPersonalizationState.current
        val mode = if (personalization.toolsPersonalized) Mode.PERSONALIZATION else Mode.ALL_TOOLS
        val filters = toolFiltersStateProducer.produce(mode)
        val selectedLocale by rememberUpdatedState(filters.languageFilter.selectedItem?.code)

        val appLocale by settings.appLanguageFlow.collectAsState()
        RegisterToolOrderSyncTask(selectedLocale ?: appLocale)
        RegisterFeaturedToolsSyncTask(appLocale)

        // featured tools always follow the app language, regardless of the selected language filter
        val featuredTools = rememberFeaturedTools { openToolDetails(it, null, SOURCE_SPOTLIGHT) }

        val tools = rememberTools(
            mode = mode,
            category = filters.categoryFilter.selectedItem,
            language = filters.languageFilter.selectedItem,
        ) { openToolDetails(it, selectedLocale, SOURCE_ALL_TOOLS) }

        return UiState(
            mode = mode,
            banner = favoriteToolsBannerPresenter.present(),
            dataLoaded = featuredTools != null && tools != null,
            spotlightTools = if (mode == Mode.PERSONALIZATION) featuredTools.orEmpty() else emptyList(),
            filters = filters,
            tools = tools.orEmpty(),
        ) {
            when (it) {
                is UiEvent.ChangeMode -> personalization.toolsPersonalized = it.mode == Mode.PERSONALIZATION
                is UiEvent.OpenLocalizationSettings -> navigator.goTo(CountrySettingsScreen)
            }
        }
    }

    @Composable
    private fun RegisterToolOrderSyncTask(locale: Locale) {
        circuitContext.rememberSyncTask(locale) { syncToolOrder(locale, it) }
    }

    @Composable
    private fun RegisterFeaturedToolsSyncTask(locale: Locale) {
        circuitContext.rememberSyncTask(locale) { syncFeaturedTools(locale, it) }
    }

    @Composable
    private fun rememberFeaturedTools(onOpenToolDetails: (String) -> Unit): List<ToolCardPresenter.UiState>? {
        val tools by remember { featuredToolsFlowProducer.getFlow() }.collectAsState(null)
        return tools?.toToolCardState(null, onOpenToolDetails)
    }

    @Composable
    private fun rememberTools(
        mode: Mode,
        category: String?,
        language: Language?,
        onOpenToolDetails: (String) -> Unit,
    ): List<ToolCardPresenter.UiState>? {
        val locale = language?.code
        val tools by remember(mode, category, locale) { filteredToolsFlowProducer.getFlow(mode, category, locale) }
            .collectAsState(null)
        return tools?.toToolCardState(language, onOpenToolDetails)
    }

    @Composable
    private fun List<Tool>.toToolCardState(language: Language?, onOpenToolDetails: (String) -> Unit) = map { tool ->
        key(tool.code) {
            val toolCode by rememberUpdatedState(tool.code)
            toolCardPresenter.present(
                tool = tool,
                secondLanguage = language,
                eventSink = {
                    when (it) {
                        ToolCardEvent.Click,
                        ToolCardEvent.OpenTool,
                        ToolCardEvent.OpenToolDetails -> toolCode?.let { onOpenToolDetails(it) }
                    }
                }
            )
        }
    }

    private fun openToolDetails(tool: String, selectedLocale: Locale?, source: String?) {
        if (source != null) {
            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, tool, source))
        }
        navigator.goTo(ToolDetailsScreen(tool, selectedLocale))
    }

    private fun SyncTracker.syncToolOrder(locale: Locale, force: Boolean = false) = launchSync {
        syncService.syncToolOrder(locale, settings.getCountrySettingFlow().first(), force)
    }

    private fun SyncTracker.syncFeaturedTools(locale: Locale, force: Boolean = false) = launchSync {
        val country = settings.getCountrySettingFlow().first() ?: return@launchSync
        syncService.syncFeaturedTools(locale, country, force)
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(circuitContext: CircuitContext, navigator: Navigator): ToolsPresenter
    }
}
