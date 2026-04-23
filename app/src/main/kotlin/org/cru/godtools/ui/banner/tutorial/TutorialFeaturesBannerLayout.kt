package org.cru.godtools.ui.banner.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cru.godtools.tutorial.R
import org.cru.godtools.ui.banner.MaterialBanner
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter.UiEvent
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter.UiState

@Composable
internal fun TutorialFeaturesBannerLayout(state: UiState, modifier: Modifier = Modifier) {
    MaterialBanner(
        text = stringResource(R.string.tutorial_features_banner_text),
        primaryButton = stringResource(R.string.tutorial_features_banner_action_open),
        primaryAction = { state.eventSink(UiEvent.OpenTutorial) },
        secondaryButton = stringResource(R.string.tutorial_features_banner_action_dismiss),
        secondaryAction = { state.eventSink(UiEvent.Dismiss) },
        modifier = modifier
    )
}
