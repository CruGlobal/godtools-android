package org.cru.godtools.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.ui.onboarding.OnboardingPresenter.UiState
import org.greenrobot.eventbus.EventBus

class OnboardingPresenter @AssistedInject constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    data class UiState(val eventSink: (UiEvent) -> Unit = {}) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object ChangeLanguage : UiEvent
        data object Skip : UiEvent
        data object Finish : UiEvent
    }

    @Composable
    override fun present(): UiState {
        LaunchedEffect(Unit) { settings.setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING) }

        return UiState { event ->
            when (event) {
                UiEvent.ChangeLanguage -> navigator.goTo(AppLanguageScreen)

                UiEvent.Skip -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP))
                    navigator.pop()
                }

                UiEvent.Finish -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH))
                    navigator.pop()
                }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(OnboardingScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): OnboardingPresenter
    }
}
