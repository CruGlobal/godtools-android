package org.cru.godtools.tutorial.layout

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
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.createCircuitActivityIntent
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.greenrobot.eventbus.EventBus

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
                UiEvent.Back -> navigator.pop(TutorialScreen.Result.Canceled)

                UiEvent.Onboarding.ChangeLanguage ->
                    navigator.goTo(IntentScreen(context.createCircuitActivityIntent(AppLanguageScreen)))

                UiEvent.Onboarding.Skip -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP))
                    navigator.pop(TutorialScreen.Result.Finished)
                }

                UiEvent.Onboarding.Finish -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH))
                    navigator.pop(TutorialScreen.Result.Finished)
                }

                UiEvent.LiveShare.QrCode -> navigator.pop(TutorialScreen.Result.ShowQrCode)

                UiEvent.Features.Finish,
                UiEvent.LiveShare.Skip,
                UiEvent.LiveShare.Finish,
                UiEvent.Tips.Skip,
                UiEvent.Tips.Finish -> navigator.pop(TutorialScreen.Result.Finished)
            }
        }
    }

    @AssistedFactory
    @CircuitInject(TutorialScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator, screen: TutorialScreen): TutorialPresenter
    }
}
