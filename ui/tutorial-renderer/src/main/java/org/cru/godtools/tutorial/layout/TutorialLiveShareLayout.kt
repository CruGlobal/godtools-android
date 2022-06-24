@file:Suppress("UNUSED_EXPRESSION")

package org.cru.godtools.tutorial.layout

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R

@Composable
internal fun TutorialLiveShareLayout(
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
    page: Page,
    anim: Int,
    title: Int,
    body: Int,
    body2: Int? = null
) = GodToolsTheme() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = dimensionResource(R.dimen.tutorial_page_inset_top),
                bottom = dimensionResource(R.dimen.tutorial_page_inset_bottom)
            ).verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth()) {
            Column() {
                Spacer(modifier = Modifier.height(290.dp))
                Spacer(modifier = Modifier.minLinesHeight(1, MaterialTheme.typography.titleLarge))
                Spacer(modifier = Modifier.minLinesHeight(8, MaterialTheme.typography.bodyMedium))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Text(
                    stringResource(body),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                if (body2 != null) {
                    Spacer(
                        modifier = Modifier.minLinesHeight(
                            minLines = 1,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    )
                    Text(
                        stringResource(body2),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                val composition by rememberLottieComposition(

                    LottieCompositionSpec.RawRes(anim)
                )

                val progress by animateLottieCompositionAsState(
                    // pass the composition created above
                    composition,

                    // Iterates Forever
                    iterations = LottieConstants.IterateForever,

                    // pass isPlaying we created above,
                    // changing isPlaying will recompose
                    // Lottie and pause/play
                    isPlaying = true,

                    // pass speed we created above,
                    // changing speed will increase Lottie
                    speed = 1f,

                    // this makes animation to restart
                    // when paused and play
                    // pass false to continue the animation
                    // at which is was paused
                    restartOnPlay = false

                )
                LottieAnimation(
                    composition,
                    { progress },
                    modifier = Modifier
                        .height(290.dp)
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f).fillMaxWidth())
        Button(
            onClick = {
                if (page == Page.LIVE_SHARE_START) {
                    onTutorialAction(R.id.action_live_share_finish)
                } else
                    nextPage()
            },
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(
                    if (page == Page.LIVE_SHARE_START) R.string.tutorial_live_share_action_start
                    else R.string.tutorial_live_share_action_continue
                )

            )
        }
    }
}
