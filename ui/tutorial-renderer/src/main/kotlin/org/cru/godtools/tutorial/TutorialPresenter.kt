package org.cru.godtools.tutorial

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.util.Locale
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.Settings
import org.cru.godtools.base.appLanguage
import org.cru.godtools.base.ui.circuit.createCircuitActivityIntent
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.base.ui.createArticlesIntent
import org.cru.godtools.base.ui.createDashboardIntent
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.greenrobot.eventbus.EventBus
import org.cru.godtools.base.ui.dashboard.Page as DashboardPage

// TODO: this should be dynamic based upon the available languages in the database
private val ARTICLES_SUPPORTED_LANGUAGES = setOf(
    Locale.forLanguageTag("bg"),
    Locale.ENGLISH,
    Locale.forLanguageTag("es"),
    Locale.FRENCH,
    Locale.forLanguageTag("lv"),
    Locale.forLanguageTag("ru"),
    Locale.forLanguageTag("vi")
)

class TutorialPresenter @AssistedInject constructor(
    @param:ApplicationContext private val context: Context,
    private val eventBus: EventBus,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: TutorialScreen,
) : Presenter<TutorialPresenter.UiState> {
    data class UiState(val pageSet: PageSet, val eventSink: (UiEvent) -> Unit = {}) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data class TutorialAction(val action: Action) : UiEvent
    }

    @Composable
    override fun present(): UiState {
        LaunchedEffect(screen.pageSet) {
            screen.pageSet.feature?.let { settings.setFeatureDiscovered(it) }
        }

        return UiState(screen.pageSet) {
            when (it) {
                is UiEvent.TutorialAction -> handleAction(it.action)
            }
        }
    }

    private fun handleAction(action: Action) {
        when (action) {
            Action.BACK -> navigator.pop(TutorialScreen.Result(Activity.RESULT_CANCELED))

            Action.ONBOARDING_CHANGE_LANGUAGE ->
                navigator.goTo(IntentScreen(context.createCircuitActivityIntent(AppLanguageScreen)))

            Action.ONBOARDING_WATCH_VIDEO -> navigator.goTo(YoutubePlayerScreen("RvhZ_wuxAgE"))

            Action.ONBOARDING_LAUNCH_ARTICLES -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_ARTICLES))
                val locale = sequenceOf(context.appLanguage, Locale.ENGLISH).filterNotNull().includeFallbacks()
                    .firstOrNull { ARTICLES_SUPPORTED_LANGUAGES.contains(it) } ?: Locale.ENGLISH
                navigator.goTo(IntentScreen(context.createArticlesIntent("es", locale)))
                navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }

            Action.ONBOARDING_LAUNCH_LESSONS -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_LESSONS))
                navigator.goTo(IntentScreen(context.createDashboardIntent(DashboardPage.LESSONS)))
                navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }

            Action.ONBOARDING_LAUNCH_TOOLS -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_TOOLS))
                navigator.goTo(IntentScreen(context.createDashboardIntent(DashboardPage.ALL_TOOLS)))
                navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }

            Action.ONBOARDING_SKIP -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP))
                navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }

            Action.ONBOARDING_FINISH -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH))
                navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }

            Action.LIVE_SHARE_QR_CODE -> navigator.pop(TutorialScreen.Result(TutorialActivity.RESULT_SHOW_QR_CODE))

            Action.FEATURES_FINISH,
            Action.LIVE_SHARE_SKIP,
            Action.LIVE_SHARE_FINISH,
            Action.TIPS_SKIP,
            Action.TIPS_FINISH -> navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
        }
    }

    @AssistedFactory
    @CircuitInject(TutorialScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator, screen: TutorialScreen): TutorialPresenter
    }
}
