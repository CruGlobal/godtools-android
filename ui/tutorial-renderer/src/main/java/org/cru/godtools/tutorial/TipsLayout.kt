package org.cru.godtools.tutorial

//import org.cru.godtools.R
import androidx.compose.foundation.layout.*
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
import com.airbnb.lottie.compose.*
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview(showBackground = true)
@Composable
internal fun TipsLayout() = GodToolsTheme() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 100.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

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
            progress,
            modifier = Modifier.height(290.dp).fillMaxWidth()
        )

        //Spacer(modifier = Modifier.height(20.dp))
        Text(text = stringResource(R.string.tutorial_tips_learn_headline),
            style = MaterialTheme.typography.titleLarge,
            //fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)

        )
        //Spacer(modifier = Modifier.height(20.dp))
        Text(stringResource(R.string.tutorial_tips_learn_text),
            style = MaterialTheme.typography.bodyLarge,
            //fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp))
    }


}
