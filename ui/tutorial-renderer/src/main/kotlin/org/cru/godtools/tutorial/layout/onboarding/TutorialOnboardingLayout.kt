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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN
import org.cru.godtools.tutorial.layout.TutorialMedia

internal val TUTORIAL_ONBOARDING_MEDIA_HEIGHT = 268.dp

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
            .constrainAs(title) { top.linkTo(positioning.title.top) }
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth()
    )
    val titleBottom = createBottomBarrier(title, positioning.title)

    val content = createRef()
    Text(
        page.content?.let { stringResource(it) }.orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .constrainAs(content) { top.linkTo(titleBottom) }
            .padding(top = 12.dp, horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth()
    )
    val contentBottom = createBottomBarrier(content, positioning.content)

    val media = createRef()
    TutorialMedia(
        page,
        modifier = Modifier
            .constrainAs(media) { top.linkTo(contentBottom) }
            .fillMaxWidth()
            .height(TUTORIAL_ONBOARDING_MEDIA_HEIGHT)
    )
    val mediaBottom = createBottomBarrier(media, positioning.media)

    constrain(positioning.chain) { bottom.linkTo(action.top) }
    Button(
        onClick = {
            when (page) {
                Page.ONBOARDING_SHARE_FINAL -> onTutorialAction(R.id.action_onboarding_finish)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .constrainAs(action) {
                linkTo(top = mediaBottom, bottom = parent.bottom, bias = 1f)
                centerHorizontallyTo(parent)
            }
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
    ) { Text(page.action?.let { stringResource(it) }.orEmpty()) }
}
