package org.cru.godtools.ui.onboarding

import androidx.annotation.RawRes
import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.tutorial.R
import org.cru.godtools.ui.onboarding.OnboardingPresenter.UiEvent

internal val ONBOARDING_PAGE_HORIZONTAL_MARGIN = 32.dp

internal enum class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val content: Int,
    @StringRes val action: Int,
    @RawRes val animation: Int,
) {
    CONVERSATIONS(
        title = R.string.tutorial_onboarding_conversations_headline,
        content = R.string.tutorial_onboarding_conversations_subhead,
        action = R.string.tutorial_onboarding_action_next,
        animation = R.raw.anim_tutorial_onboarding_guys,
    ),
    PREPARE(
        title = R.string.tutorial_onboarding_prepare_headline,
        content = R.string.tutorial_onboarding_prepare_subhead,
        action = R.string.tutorial_onboarding_action_next,
        animation = R.raw.anim_tutorial_onboarding_dog,
    ),
    SHARE(
        title = R.string.tutorial_onboarding_share_headline,
        content = R.string.tutorial_onboarding_share_subhead,
        action = R.string.tutorial_onboarding_action_start,
        animation = R.raw.anim_tutorial_onboarding_distance,
    ),
}

@Composable
internal fun OnboardingPageLayout(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    eventSink: (UiEvent) -> Unit = {},
) = ConstraintLayout(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val positioning = createOnboardingPositioning()

    val title = createRef()
    Text(
        text = stringResource(page.title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .constrainAs(title) { top.linkTo(positioning.title.top) }
            .padding(horizontal = ONBOARDING_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth()
    )
    val titleBottom = createBottomBarrier(title, positioning.title)

    val content = createRef()
    Text(
        stringResource(page.content),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .constrainAs(content) { top.linkTo(titleBottom) }
            .padding(top = 12.dp, horizontal = ONBOARDING_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth()
    )
    val contentBottom = createBottomBarrier(content, positioning.content)

    val media = createRef()
    OnboardingAnimation(
        animation = page.animation,
        modifier = Modifier
            .constrainAs(media) { top.linkTo(contentBottom) }
            .fillMaxWidth()
            .height(ONBOARDING_MEDIA_HEIGHT)
    )
    val mediaBottom = createBottomBarrier(media, positioning.media)

    val action = createRef()
    constrain(positioning.chain) { bottom.linkTo(action.top) }
    Button(
        onClick = {
            when (page) {
                OnboardingPage.SHARE -> eventSink(UiEvent.Finish)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .constrainAs(action) {
                linkTo(top = mediaBottom, bottom = parent.bottom, bias = 1f)
                centerHorizontallyTo(parent)
            }
            .padding(horizontal = ONBOARDING_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
    ) { Text(stringResource(page.action)) }
}

@Composable
private fun OnboardingAnimation(@RawRes animation: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))
    val progress by animateLottieCompositionAsState(
        composition,
        restartOnPlay = false,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(composition, { progress }, modifier = modifier)
}
