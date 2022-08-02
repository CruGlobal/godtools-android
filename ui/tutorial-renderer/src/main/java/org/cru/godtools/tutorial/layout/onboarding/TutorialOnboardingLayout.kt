package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN
import org.cru.godtools.tutorial.layout.TutorialMedia

@Composable
internal fun TutorialOnboardingLayout(
    page: Page,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = ConstraintLayout(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val positioning = createTutorialOnboardingPositioning()
    val action = createRef()

    val title = createRef()
    Text(
        text = page.title?.let { stringResource(it) }.orEmpty(),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .constrainAs(title) {
                top.linkTo(positioning.title.top)
                centerHorizontallyTo(parent)
            }
    )
    val titleBottom = createBottomBarrier(title, positioning.title)

    val content = createRef()
    Text(
        page.content?.let { stringResource(it) }.orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .constrainAs(content) {
                top.linkTo(titleBottom, margin = 12.dp)
                centerHorizontallyTo(parent)
            }
    )
    val contentBottom = createBottomBarrier(content, positioning.content)

    TutorialMedia(
        page,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.tutorial_page_onboarding_anim_height))
            .constrainAs(createRef()) { linkTo(top = contentBottom, bottom = action.top, bias = 0f) }
    )

    constrain(positioning.chain) { bottom.linkTo(action.top) }
    Button(
        onClick = {
            when (page) {
                Page.ONBOARDING_SHARE_FINAL -> onTutorialAction(R.id.action_onboarding_finish)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
            .constrainAs(action) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom)
            }
    ) { Text(page.action?.let { stringResource(it) }.orEmpty()) }
}
