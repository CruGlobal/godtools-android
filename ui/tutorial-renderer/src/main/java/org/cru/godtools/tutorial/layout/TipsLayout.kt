@file:Suppress("UNUSED_EXPRESSION")

package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
/*
column, box column
spacer for top with weight 1
column for middle content
box for button with weight 1
 */
@Composable
internal fun TipsTutorialLayout(
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (page == Page.TIPS_START) {
                Button(
                    onClick = {
                        onTutorialAction(R.id.action_tips_finish)
                    },

                    modifier = Modifier.padding(horizontal = 32.dp).width(275.dp).align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = stringResource(id = R.string.tutorial_tips_action_start),
                        fontSize = 30.sp
                    )
                }
            } else {
                Button(
                    onClick = nextPage,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .width(250.dp).align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = stringResource(id = R.string.tutorial_tips_action_continue),
                        fontSize = 30.sp
                    )
                }
            }
        }
    }
}
