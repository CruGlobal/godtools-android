package org.cru.godtools.tutorial.layout

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
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivity
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
        data object Back : UiEvent

        sealed interface Features : UiEvent {
            data object Finish : Features
        }

        sealed interface LiveShare : UiEvent {
            data object Skip : LiveShare
            data object QrCode : LiveShare
            data object Finish : LiveShare
        }

        sealed interface Onboarding : UiEvent {
            data object ChangeLanguage : Onboarding
            data object Skip : Onboarding
            data object LaunchArticles : Onboarding
            data object LaunchLessons : Onboarding
            data object LaunchTools : Onboarding
            data object Finish : Onboarding
        }

        sealed interface Tips : UiEvent {
            data object Skip : Tips
            data object Finish : Tips
        }
    }

    @Composable
    override fun present(): UiState {
        LaunchedEffect(screen.pageSet) {
            screen.pageSet.feature?.let { settings.setFeatureDiscovered(it) }
        }

        return UiState(screen.pageSet) { event ->
            when (event) {
                UiEvent.Back -> navigator.pop(TutorialScreen.Result(Activity.RESULT_CANCELED))

                UiEvent.Onboarding.ChangeLanguage ->
                    navigator.goTo(IntentScreen(context.createCircuitActivityIntent(AppLanguageScreen)))

                UiEvent.Onboarding.LaunchArticles -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_ARTICLES))
                    val locale = sequenceOf(context.appLanguage, Locale.ENGLISH).filterNotNull().includeFallbacks()
                        .firstOrNull { ARTICLES_SUPPORTED_LANGUAGES.contains(it) } ?: Locale.ENGLISH
                    navigator.goTo(IntentScreen(context.createArticlesIntent("es", locale)))
                    navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
                }

                UiEvent.Onboarding.LaunchLessons -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_LESSONS))
                    navigator.goTo(IntentScreen(context.createDashboardIntent(DashboardPage.LESSONS)))
                    navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
                }

                UiEvent.Onboarding.LaunchTools -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_TOOLS))
                    navigator.goTo(IntentScreen(context.createDashboardIntent(DashboardPage.ALL_TOOLS)))
                    navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
                }

                UiEvent.Onboarding.Skip -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP))
                    navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
                }

                UiEvent.Onboarding.Finish -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH))
                    navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
                }

                UiEvent.LiveShare.QrCode -> navigator.pop(TutorialScreen.Result(TutorialActivity.RESULT_SHOW_QR_CODE))

                UiEvent.Features.Finish,
                UiEvent.LiveShare.Skip,
                UiEvent.LiveShare.Finish,
                UiEvent.Tips.Skip,
                UiEvent.Tips.Finish -> navigator.pop(TutorialScreen.Result(Activity.RESULT_OK))
            }
        }
    }

    @AssistedFactory
    @CircuitInject(TutorialScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator, screen: TutorialScreen): TutorialPresenter
    }
}
