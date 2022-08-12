package org.cru.godtools.tutorial.layout.tips

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.cru.godtools.tutorial.Action
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN

@Composable
internal fun TutorialTipsLayout(
    page: Page,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Action) -> Unit = {}
) = Column(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    Spacer(modifier = Modifier.weight(1f))

    when {
        page.animation != null -> {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.animation))

            val progress by animateLottieCompositionAsState(
                composition,
                restartOnPlay = false,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                composition,
                { progress },
                modifier = Modifier
                    .height(290.dp)
                    .fillMaxWidth()
            )
        }
        else -> Spacer(modifier = Modifier.height(290.dp))
    }

    Box(modifier = Modifier.padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)) {
        val titleStyle = MaterialTheme.typography.titleLarge
        val contentStyle = MaterialTheme.typography.bodyMedium

        Column {
            Text(
                page.title?.let { stringResource(it) }.orEmpty(),
                style = titleStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                page.content?.let { stringResource(it) }.orEmpty(),
                style = contentStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )

            if (page.content2 != null) {
                val lineHeight =
                    computeHeightForDefaultText(contentStyle, 2) - computeHeightForDefaultText(contentStyle, 1)
                Spacer(modifier = Modifier.height(lineHeight))
                Text(
                    stringResource(page.content2),
                    style = contentStyle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column {
            Spacer(modifier = Modifier.minLinesHeight(1, titleStyle))
            Spacer(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .minLinesHeight(8, contentStyle)
            )
        }
    }

    Spacer(modifier = Modifier.weight(1f))
    Button(
        onClick = {
            when (page) {
                Page.TIPS_START -> onTutorialAction(Action.TIPS_FINISH)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally)
    ) {
        Text(
            stringResource(
                when (page) {
                    Page.TIPS_START -> R.string.tutorial_tips_action_start
                    else -> R.string.tutorial_tips_action_continue
                }
            )
        )
    }
}
