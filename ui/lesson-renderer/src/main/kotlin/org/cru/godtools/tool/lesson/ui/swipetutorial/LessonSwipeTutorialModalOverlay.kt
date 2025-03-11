package org.cru.godtools.tool.lesson.ui.swipetutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.slack.circuit.overlay.AnimatedOverlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.tool.lesson.R

class LessonSwipeTutorialAnimatedModalOverlay :
    AnimatedOverlay<Unit>(enterTransition = fadeIn(), exitTransition = fadeOut()) {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(navigator: OverlayNavigator<Unit>) {
        BackHandler { navigator.finish(Unit) }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .clickable(null, null) { navigator.finish(Unit) }
                .padding(horizontal = 32.dp)
                .fillMaxSize()
        ) {
            Text(
                stringResource(R.string.lesson_tutorial_swipe_message),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
            )

            val anim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_lesson_tutorial_page_swipe))
            val progress by animateLottieCompositionAsState(
                anim,
                restartOnPlay = false,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(anim, { progress }, modifier = Modifier.height(300.dp))

            Button(
                onClick = { navigator.finish(Unit) },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(stringResource(R.string.lesson_tutorial_swipe_action_ok))
            }
        }
    }
}
