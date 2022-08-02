package org.cru.godtools.tutorial.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.layout.features.TutorialFeaturesLayout
import org.cru.godtools.tutorial.layout.liveshare.TutorialLiveShareLayout
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingLayout
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingLinksLayout
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingWelcomeLayout
import org.cru.godtools.tutorial.layout.tips.TutorialTipsLayout

internal val TUTORIAL_PAGE_HORIZONTAL_MARGIN = 32.dp

@Composable
internal fun TutorialPageLayout(
    page: Page,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = when (page) {
    Page.FEATURES_LESSONS,
    Page.FEATURES_TOOLS,
    Page.FEATURES_TIPS,
    Page.FEATURES_LIVE_SHARE,
    Page.FEATURES_FINAL -> TutorialFeaturesLayout(
        page,
        nextPage = nextPage,
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    Page.LIVE_SHARE_DESCRIPTION,
    Page.LIVE_SHARE_MIRRORED,
    Page.LIVE_SHARE_START -> TutorialLiveShareLayout(
        page,
        nextPage = nextPage,
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    Page.ONBOARDING_WELCOME -> TutorialOnboardingWelcomeLayout(
        nextPage = nextPage,
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    Page.ONBOARDING_CONVERSATIONS,
    Page.ONBOARDING_PREPARE,
    Page.ONBOARDING_SHARE,
    Page.ONBOARDING_SHARE_FINAL -> TutorialOnboardingLayout(
        page,
        nextPage = nextPage,
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    Page.ONBOARDING_LINKS -> TutorialOnboardingLinksLayout(
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    Page.TIPS_LEARN,
    Page.TIPS_LIGHT,
    Page.TIPS_START -> TutorialTipsLayout(
        page,
        nextPage = nextPage,
        onTutorialAction = onTutorialAction,
        modifier = modifier,
    )
    else -> Unit
}
