package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.cru.godtools.tutorial.Page

@Composable
internal fun TutorialMedia(
    page: Page,
    modifier: Modifier = Modifier,
    imageAlignment: Alignment = Alignment.TopCenter,
    imageContentScale: ContentScale = ContentScale.Inside
) = when {
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
            modifier = modifier
        )
    }
    page.image != null -> Image(
        painter = painterResource(page.image),
        contentDescription = null,
        alignment = imageAlignment,
        contentScale = imageContentScale,
        modifier = modifier
    )
    else -> Spacer(modifier)
}
