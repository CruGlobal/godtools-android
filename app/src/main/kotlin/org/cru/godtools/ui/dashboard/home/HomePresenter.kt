package org.cru.godtools.ui.dashboard.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import org.cru.godtools.BuildConfig
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FEATURED
import org.cru.godtools.base.CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter
import org.cru.godtools.ui.dashboard.home.HomePresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsScreen
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus

class HomePresenter @AssistedInject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val remoteConfig: FirebaseRemoteConfig,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    private val tutorialFeaturesBannerPresenter: BannerPresenter<TutorialFeaturesBannerPresenter.UiState>,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    data class UiState(
        val dataLoaded: Boolean = true,
        val banner: Banner.UiState? = null,
        val spotlightLessons: List<ToolCard.State> = emptyList(),
        val favoriteTools: List<ToolCard.State> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object ViewAllFavorites : UiEvent
        data object ViewAllTools : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val spotlightLessons = rememberSpotlightLessons()
        val favoriteTools = rememberFavoriteTools()

        return UiState(
            dataLoaded = spotlightLessons != null && favoriteTools != null,
            banner = tutorialFeaturesBannerPresenter.present(),
            spotlightLessons = spotlightLessons.orEmpty(),
            favoriteTools = favoriteTools.orEmpty(),
        ) {
            when (it) {
                UiEvent.ViewAllFavorites -> navigator.goTo(AllFavoritesScreen)
                UiEvent.ViewAllTools -> navigator.resetRoot(ToolsScreen, Navigator.StateOptions.SaveAndRestore)
            }
        }
    }

    @Composable
    private fun rememberSpotlightLessons() =
        remember { toolsRepository.getLessonsFlow().map { it.filter { !it.isHidden && it.isSpotlight } } }
            .collectAsState(null).value
            ?.mapNotNull { lesson ->
                val lessonCode = lesson.code ?: return@mapNotNull null

                key(lessonCode) {
                    lateinit var lessonState: ToolCard.State
                    lessonState = toolCardPresenter.present(lesson) {
                        when (it) {
                            ToolCard.Event.Click -> {
                                val intent = lesson.createToolIntent(
                                    context = context,
                                    languages = listOfNotNull(lessonState.translation?.languageCode),
                                    resumeProgress = true,
                                )

                                if (intent != null) {
                                    eventBus.post(
                                        OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, lessonCode, SOURCE_FEATURED)
                                    )
                                    navigator.goTo(IntentScreen(intent))
                                }
                            }

                            else -> if (BuildConfig.DEBUG) error("$it is currently unsupported for Lesson Cards")
                        }
                    }
                    lessonState
                }
            }

    @Composable
    private fun rememberFavoriteTools() = remember {
        toolsRepository.getFavoriteToolsFlow()
            .map {
                it.take(
                    remoteConfig.getLong(CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS)
                        .coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
                )
            }
    }.collectAsState(null).value
        ?.mapNotNull { tool ->
            val toolCode = tool.code ?: return@mapNotNull null

            key(toolCode) {
                lateinit var state: ToolCard.State
                state = toolCardPresenter.present(tool) {
                    when (it) {
                        ToolCard.Event.Click,
                        ToolCard.Event.OpenTool -> {
                            val intent = tool.createToolIntent(
                                context = context,
                                languages = listOfNotNull(
                                    tool.primaryLocale ?: state.translation?.languageCode,
                                    tool.parallelLocale
                                ),
                                saveLanguageSettings = true
                            )

                            if (intent != null) {
                                eventBus.post(
                                    OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, tool.code, SOURCE_FAVORITE)
                                )
                                navigator.goTo(IntentScreen(intent))
                            }
                        }

                        ToolCard.Event.OpenToolDetails -> {
                            eventBus.post(
                                OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, toolCode, SOURCE_FAVORITE)
                            )
                            navigator.goTo(ToolDetailsScreen(toolCode))
                        }

                        ToolCard.Event.PinTool,
                        ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                    }
                }
                state
            }
        }

    @AssistedFactory
    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
