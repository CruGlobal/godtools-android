package org.cru.godtools.tutorial
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
//import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import java.nio.file.Files.size

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
