package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingLayout
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingLinksLayout
import org.cru.godtools.tutorial.layout.onboarding.TutorialOnboardingWelcomeLayout
import org.cru.godtools.tutorial.layout.tips.TutorialTipsLayout

internal val TUTORIAL_PAGE_HORIZONTAL_MARGIN = 32.dp

@Composable
internal fun TutorialPageLayout(
    page: Page,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = Box(
    modifier = Modifier
        .padding(
            top = dimensionResource(R.dimen.tutorial_page_inset_top),
            bottom = dimensionResource(R.dimen.tutorial_page_inset_bottom)
        )
) {
    when (page) {
        Page.ONBOARDING_WELCOME -> TutorialOnboardingWelcomeLayout(nextPage, onTutorialAction)
        Page.ONBOARDING_CONVERSATIONS,
        Page.ONBOARDING_PREPARE,
        Page.ONBOARDING_SHARE,
        Page.ONBOARDING_SHARE_FINAL -> TutorialOnboardingLayout(page, nextPage, onTutorialAction)
        Page.ONBOARDING_LINKS -> TutorialOnboardingLinksLayout(onTutorialAction = onTutorialAction)
        Page.TIPS_LEARN,
        Page.TIPS_LIGHT,
        Page.TIPS_START -> TutorialTipsLayout(page, nextPage = nextPage, onTutorialAction = onTutorialAction)
        else -> Unit
    }
}
