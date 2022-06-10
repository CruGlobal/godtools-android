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
import androidx.compose.ui.text.style.TextAlign

//import com.airbnb.lottie.compose.*
//import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import java.nio.file.Files.size

@Preview(showBackground = true)
@Composable
internal fun TipsLayout() = GodToolsTheme() {
    Column(
        modifier = Modifier.fillMaxSize(),
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(100.dp))
        /*val composition by rememberLottieComposition(
            LottieCompositionSpec
                .RawRes(org.cru.godtools.tutorial.R.raw.anim_tutorial_features_tips)
        )
        var isPlaying by remember {
            mutableStateOf(true)
        }

// for speed
        var speed by remember {
            mutableStateOf(1f)
        }
        val progress by animateLottieCompositionAsState(
            // pass the composition created above
            composition,

            // Iterates Forever
            iterations = LottieConstants.IterateForever,

            // pass isPlaying we created above,
            // changing isPlaying will recompose
            // Lottie and pause/play
            isPlaying = isPlaying,

            // pass speed we created above,
            // changing speed will increase Lottie
            speed = speed,

            // this makes animation to restart
            // when paused and play
            // pass false to continue the animation
            // at which is was paused
            restartOnPlay = false

        )
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.size(250.dp)
        )*/
        Image(
            painter = painterResource(R.drawable.img_tutorial_live_share_people),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                // Set image size to 40 dp
                .size(250.dp)
                // Clip image to be shaped as a circle

        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Learn to share this tool with anyone",
            //style = MaterialTheme.typography.subtitle2,
            fontSize = 37.sp,
            textAlign = TextAlign.Center
            //modifier = Modifier.align

        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Practice using as many tips as you need to feel confident in sharing this tool with others.",
            fontSize = 25.sp,
            textAlign = TextAlign.Center)
    }


}
