package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.tutorial.startTutorialActivity
import org.greenrobot.eventbus.EventBus

@Composable
internal fun TutorialFeaturesBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel = viewModel<TutorialFeaturesBannerViewModel>()

    Banner(
        text = stringResource(R.string.tutorial_features_banner_text),
        primaryButton = stringResource(R.string.tutorial_features_banner_action_open),
        primaryAction = { context.startTutorialActivity(PageSet.FEATURES) },
        secondaryButton = stringResource(R.string.tutorial_features_banner_action_dismiss),
        secondaryAction = { viewModel.dismiss() },
        modifier = modifier
    )
}

@HiltViewModel
internal class TutorialFeaturesBannerViewModel @Inject constructor(val eventBus: EventBus, val settings: Settings) :
    ViewModel() {
    fun dismiss() {
        eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.BANNER_DISMISS))
        settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_FEATURES)
    }
}
