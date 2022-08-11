package org.cru.godtools.tutorial.layout.features

import androidx.compose.foundation.layout.Spacer
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
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN
import org.cru.godtools.tutorial.layout.TutorialMedia
import org.cru.godtools.tutorial.layout.TutorialPositionReferences

private val TUTORIAL_FEATURES_MEDIA_HEIGHT = 252.dp

@Composable
internal fun TutorialFeaturesLayout(
    page: Page,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = ConstraintLayout(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val positioning = createTutorialFeaturesPositioning()
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
    if (page.content != null) {
        Text(
            stringResource(page.content),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(content) { top.linkTo(titleBottom) }
                .padding(top = 12.dp, horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
                .fillMaxWidth()
        )
    }
    val contentBottom = createBottomBarrier(content, positioning.content)

    val media = createRef()
    TutorialMedia(
        page,
        modifier = Modifier
            .constrainAs(media) { top.linkTo(contentBottom) }
            .fillMaxWidth()
            .height(TUTORIAL_FEATURES_MEDIA_HEIGHT)
    )
    val mediaBottom = createBottomBarrier(media, positioning.media)

    constrain(positioning.chain) { bottom.linkTo(action.top) }
    Button(
        onClick = {
            when (page) {
                Page.FEATURES_FINAL -> onTutorialAction(R.id.action_features_finish)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .constrainAs(action) {
                centerHorizontallyTo(parent)
                linkTo(mediaBottom, parent.bottom, bias = 1f)
            }
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
    ) { Text(page.action?.let { stringResource(it) }.orEmpty()) }
}

@Composable
private fun ConstraintLayoutScope.createTutorialFeaturesPositioning(): TutorialPositionReferences {
    val (title, content, media) = createRefs()
    val chain = createVerticalChain(title, content, media, chainStyle = ChainStyle.Packed)

    Spacer(
        modifier = Modifier
            .constrainAs(title) {
                top.linkTo(parent.top)
                bottom.linkTo(content.top)
            }
            .minLinesHeight(1, MaterialTheme.typography.headlineMedium)
    )
    Spacer(
        modifier = Modifier
            .constrainAs(content) {
                top.linkTo(title.bottom)
                bottom.linkTo(media.top)
            }
            .padding(top = 12.dp)
            .minLinesHeight(4, MaterialTheme.typography.bodyLarge)
    )
    Spacer(
        modifier = Modifier
            .constrainAs(media) {
                top.linkTo(content.bottom)
                bottom.linkTo(parent.bottom)
            }
            .height(TUTORIAL_FEATURES_MEDIA_HEIGHT)
    )

    return TutorialPositionReferences(title, content, media, chain)
}
