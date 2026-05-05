package org.cru.godtools.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.rememberAnsweringNavigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.ui.onboarding.OnboardingPresenter.UiState
import org.cru.godtools.ui.settings.country.CountrySettingsScreen
import org.greenrobot.eventbus.EventBus

class OnboardingPresenter @AssistedInject constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    data class UiState(
        val pagerState: PagerState,
        val userScrollEnabled: Boolean = false,
        val eventSink: (UiEvent) -> Unit = {}
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object ChangeLanguage : UiEvent
        data object Next : UiEvent
        data object Skip : UiEvent
        data object Finish : UiEvent
    }

    private suspend fun PagerState.navigateToPage(page: Int = currentPage + 1) {
        animateScrollToPage(page, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    }

    @Composable
    override fun present(): UiState {
        var languageWasSet by rememberSaveable { mutableStateOf(false) }
        var hasSeenCountrySettings by rememberSaveable { mutableStateOf(false) }
        val pagerState = rememberPagerState(pageCount = { 4 })
        val scope = rememberCoroutineScope()
        val countrySettingsNavigator = rememberAnsweringNavigator<CountrySettingsScreen.Result>(navigator) {
            hasSeenCountrySettings = true
            scope.launch {
                pagerState.navigateToPage(1)
            }
        }
        val languageButtonNavigator = rememberAnsweringNavigator<AppLanguageScreen.Result>(navigator) { result ->
            if (result is AppLanguageScreen.Result.LanguageSelected) {
                languageWasSet = true
            }
        }
        val nextButtonNavigator = rememberAnsweringNavigator<AppLanguageScreen.Result>(navigator) { result ->
            if (result is AppLanguageScreen.Result.LanguageSelected) {
                languageWasSet = true
                countrySettingsNavigator.goTo(CountrySettingsScreen)
            }
        }
        LaunchedEffect(Unit) { settings.setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING) }
        return UiState(
            pagerState = pagerState,
            userScrollEnabled = (languageWasSet && hasSeenCountrySettings) || pagerState.currentPage > 0
        ) { event ->
            when (event) {
                UiEvent.ChangeLanguage -> languageButtonNavigator.goTo(AppLanguageScreen)

                UiEvent.Next -> when {
                    !languageWasSet -> nextButtonNavigator.goTo(AppLanguageScreen)
                    !hasSeenCountrySettings -> countrySettingsNavigator.goTo(CountrySettingsScreen)
                    else -> scope.launch { pagerState.navigateToPage() }
                }

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
