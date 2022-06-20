@file:Suppress("UNUSED_EXPRESSION")

package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.R

@Composable
internal fun TipsTutorialLayout(
    nextPage: () -> Unit = {},
    onTutorialAction: (Int?) -> Unit = {},
    anim: Int,
    title: Int,
    body: Int,
    body2: Int? = null
) = GodToolsTheme() {

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 70.dp),
        verticalArrangement = Arrangement.Bottom,
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
            modifier = Modifier.height(290.dp).fillMaxWidth()
        )

        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,

        )

        Text(
            stringResource(body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (body2 != null) {

            Text(
                // Spacer(modifier = Modifier.minLinesHeight(minLines = 1, textStyle = MaterialTheme.typography.bodyMedium)),
                stringResource(body2),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        if (anim == R.raw.anim_tutorial_tips_light) {
            Spacer(Modifier.padding(top = 150.dp))
            Button(
                onClick = {
                    onTutorialAction(R.id.action_tips_finish)
                },

                modifier = Modifier.width(250.dp)
            ) {
                Text(
                    text = "Start Training",
                    fontSize = 30.sp
                )
            }
        } else {
            Button(
                onClick = nextPage,
                modifier = Modifier.padding(top = 150.dp).width(250.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 30.sp
                )
            }
        }
    }
}
