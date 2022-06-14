package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.R

@Preview(showBackground = true)
@Composable
internal fun TipsTutorialLayout() = GodToolsTheme() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec
                .RawRes(org.cru.godtools.tutorial.R.raw.anim_tutorial_tips_people)
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
            text = stringResource(R.string.tutorial_tips_learn_headline),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,

        )

        Text(
            stringResource(R.string.tutorial_tips_learn_text),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
