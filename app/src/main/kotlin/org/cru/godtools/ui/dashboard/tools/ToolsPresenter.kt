package org.cru.godtools.ui.dashboard.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.base.CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.model.Language
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.syncTaskRegistry
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.greenrobot.eventbus.EventBus

class ToolsPresenter @AssistedInject internal constructor(
    private val eventBus: EventBus,
    private val remoteConfig: FirebaseRemoteConfig,
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
        // TODO: temporary until personalization is rolled out to everyone,
        //       then this can be removed and the mode logic simplified
        val isPersonalizationEnabled: Boolean = false,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState {
        enum class Mode { PERSONALIZATION, ALL_TOOLS }
    }

    sealed interface UiEvent : CircuitUiEvent {
        data class ChangeMode(val mode: Mode) : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val isPersonalizationEnabled = rememberSaveable {
            remoteConfig.getBoolean(CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED)
        }
        var mode by rememberSaveable {
            mutableStateOf(if (isPersonalizationEnabled) Mode.PERSONALIZATION else Mode.ALL_TOOLS)
        }
        val filters = toolFiltersStateProducer.produce(mode)
        val selectedLocale by rememberUpdatedState(filters.languageFilter.selectedItem?.code)

        RegisterSyncTask(selectedLocale)

        val featuredTools = rememberFeaturedTools(
            mode = mode,
            language = filters.languageFilter.selectedItem
        ) { openToolDetails(it, selectedLocale, SOURCE_SPOTLIGHT) }

        val tools = rememberTools(
            mode = mode,
            category = filters.categoryFilter.selectedItem,
            language = filters.languageFilter.selectedItem,
        ) { openToolDetails(it, selectedLocale, SOURCE_ALL_TOOLS) }

        return UiState(
            mode = mode,
            banner = favoriteToolsBannerPresenter.present(),
            dataLoaded = featuredTools != null && tools != null,
            spotlightTools = when {
                isPersonalizationEnabled && mode == Mode.ALL_TOOLS -> emptyList()
                else -> featuredTools.orEmpty()
            },
            filters = filters,
            tools = tools.orEmpty(),
            isPersonalizationEnabled = isPersonalizationEnabled,
        ) {
            when (it) {
                is UiEvent.ChangeMode -> mode = it.mode
            }
        }
    }

    @Composable
    private fun RegisterSyncTask(selectedLocale: Locale?) {
        val syncRegistry = circuitContext.syncTaskRegistry
        DisposableEffect(syncRegistry, selectedLocale) {
            if (syncRegistry == null) return@DisposableEffect onDispose { }
            val id = syncRegistry.registerSyncTask { force -> syncData(selectedLocale ?: settings.appLanguage, force) }
            onDispose { syncRegistry.unregisterSyncTask(id) }
        }
    }

    @Composable
    private fun rememberFeaturedTools(
        mode: Mode,
        language: Language?,
        onOpenToolDetails: (String) -> Unit,
    ): List<ToolCardPresenter.UiState>? {
        val locale = language?.code
        val tools by remember(mode, locale) {
            featuredToolsFlowProducer.getFlow(mode, locale)
        }.collectAsState(null)

        return tools?.map { tool ->
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

        return tools?.map { tool ->
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
    }

    private fun openToolDetails(tool: String, selectedLocale: Locale?, source: String?) {
        if (source != null) {
            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, tool, source))
        }
        navigator.goTo(ToolDetailsScreen(tool, selectedLocale))
    }

    private fun SyncTracker.syncData(locale: Locale, force: Boolean = false) = launchSync {
        val country = settings.getCountrySettingFlow().first()
        syncService.syncToolOrder(locale, country, force)
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(circuitContext: CircuitContext, navigator: Navigator): ToolsPresenter
    }
}
