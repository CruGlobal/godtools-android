package org.cru.godtools.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.tutorial.layout.TutorialLayout
import org.cru.godtools.tutorial.theme.GodToolsTutorialTheme

@Composable
@CircuitInject(TutorialScreen::class, SingletonComponent::class)
fun TutorialCircuitLayout(state: TutorialPresenter.UiState, modifier: Modifier = Modifier) {
    GodToolsTutorialTheme {
        TutorialLayout(
            pageSet = state.pageSet,
            onTutorialAction = { state.eventSink(TutorialPresenter.UiEvent.TutorialAction(it)) },
            modifier = modifier,
        )
    }
}
