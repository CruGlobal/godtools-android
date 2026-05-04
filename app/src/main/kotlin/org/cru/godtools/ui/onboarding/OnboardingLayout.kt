package org.cru.godtools.ui.onboarding

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarAction
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarActionButton
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.layout.TutorialAppBar
import org.cru.godtools.tutorial.layout.TutorialPagerIndicator
import org.cru.godtools.tutorial.theme.TutorialThemeOverlay
import org.cru.godtools.tutorial.theme.tutorialBackgroundColor
import org.cru.godtools.ui.onboarding.OnboardingPresenter.UiEvent
import org.cru.godtools.ui.onboarding.analytics.model.OnboardingAnalyticsScreenEvent

@Composable
@CircuitInject(OnboardingScreen::class, SingletonComponent::class)
fun OnboardingLayout(state: OnboardingPresenter.UiState, modifier: Modifier = Modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)

    val coroutineScope = rememberCoroutineScope()
    val pagerState = state.pagerState

    RecordAnalyticsScreen(OnboardingAnalyticsScreenEvent(pagerState.currentPage))

    TutorialThemeOverlay {
        Scaffold(
            topBar = {
                TutorialAppBar(
                    showUpNavigation = false,
                    actions = {
                        val showMenu by remember { derivedStateOf { pagerState.currentPage in 1..2 } }
                        if (showMenu) {
                            AppBarActionButton(
                                AppBarAction(titleRes = R.string.tutorial_onboarding_action_skip),
                                onClick = { eventSink(UiEvent.Skip) },
                            )
                        }
                    },
                )
            },
            bottomBar = {
                TutorialPagerIndicator(
                    pagerState,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                )
            },
            containerColor = GodToolsTheme.tutorialBackgroundColor,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = modifier,
        ) { insets ->
            HorizontalPager(
                key = { it },
                state = pagerState,
                userScrollEnabled = state.userScrollEnabled,
                modifier = Modifier
                    .padding(insets)
                    .consumeWindowInsets(insets)
                    .fillMaxSize()
            ) {
                when (it) {
                    0 -> OnboardingWelcomePageLayout(
                        nextPage = { eventSink(UiEvent.Next) },
                        eventSink = eventSink,
                    )

                    1 -> OnboardingPageLayout(
                        OnboardingPage.CONVERSATIONS,
                        nextPage = { eventSink(UiEvent.Next) },
                        eventSink = eventSink,
                    )

                    2 -> OnboardingPageLayout(
                        OnboardingPage.PREPARE,
                        nextPage = { eventSink(UiEvent.Next) },
                        eventSink = eventSink,
                    )

                    3 -> OnboardingPageLayout(
                        OnboardingPage.SHARE,
                        nextPage = {},
                        eventSink = eventSink,
                    )
                }
            }
        }
    }
}
